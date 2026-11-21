package br.unitins.tp2.util;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import br.unitins.tp2.exception.ValidationException;

// Regras das imagens dos planos, sem banco e sem S3 (testaveis isoladamente).
// O tipo vem SEMPRE do conteudo do arquivo (magic bytes), nunca da extensao nem do Content-Type
// informados pelo cliente: um .txt renomeado para .png nao passa.
public final class RegrasImagem {

    // 2 MiB (2 097 152 bytes), mostrado ao usuario como "2 MB". O front usa o mesmo numero.
    public static final long LIMITE_BYTES = 2 * 1024 * 1024;

    // campo dos erros de validacao: o mesmo nome da parte multipart (o front mostra o erro nele)
    public static final String CAMPO = "arquivo";

    private static final byte[] ASSINATURA_JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] ASSINATURA_PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
    // WebP: "RIFF" + 4 bytes de tamanho + "WEBP"
    private static final byte[] RIFF = { 'R', 'I', 'F', 'F' };
    private static final byte[] WEBP = { 'W', 'E', 'B', 'P' };

    private static final int MAX_NOME = 100;
    private static final String NOME_PADRAO = "imagem-plano";

    private RegrasImagem() {
    }

    // tamanho informado pelo upload, conferido ANTES de ler o arquivo para a memoria
    public static void validarTamanho(long tamanho) {
        if (tamanho <= 0)
            throw ValidationException.of(CAMPO, "O arquivo está vazio.");
        if (tamanho > LIMITE_BYTES)
            throw ValidationException.of(CAMPO, "A imagem pode ter no máximo 2 MB.");
    }

    // valida tamanho e tipo do conteudo e devolve o tipo detectado (o unico em que o back confia)
    public static TipoImagem validar(byte[] conteudo) {
        validarTamanho(conteudo == null ? 0 : conteudo.length);
        return detectar(conteudo)
                .orElseThrow(() -> ValidationException.of(CAMPO, "Envie uma imagem JPEG, PNG ou WebP."));
    }

    // chave do objeto no bucket: planos/{id}/{uuid}.{ext}. Cada envio gera uma chave nova, entao trocar
    // a imagem nunca sobrescreve o objeto que um navegador ainda tem em cache.
    public static String chave(long idPlano, TipoImagem tipo) {
        return chave(idPlano, tipo, UUID.randomUUID());
    }

    static String chave(long idPlano, TipoImagem tipo, UUID uuid) {
        return "planos/" + idPlano + "/" + uuid + "." + tipo.getExtensao();
    }

    // versao da imagem = o uuid da chave. Vai no ETag e no ?v= da URL; a chave em si nunca sai da API.
    public static String versao(String chave) {
        if (chave == null || chave.isBlank())
            return null;
        String arquivo = chave.substring(chave.lastIndexOf('/') + 1);
        int ponto = arquivo.indexOf('.');
        return ponto < 0 ? arquivo : arquivo.substring(0, ponto);
    }

    // nome do arquivo para guardar e para o download: sem caminho (C:\\fakepath\\, ../), sem caracteres de
    // controle (CR/LF quebrariam o header), sem aspas nem barras, base com no maximo 100 caracteres e com a
    // extensao do tipo DETECTADO (um JPEG enviado como foto.png vira foto.jpg)
    public static String nomeParaDownload(String nomeOriginal, TipoImagem tipo) {
        String nome = nomeOriginal == null ? "" : nomeOriginal;
        nome = nome.substring(Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\')) + 1);
        nome = nome.replaceAll("[\\p{Cntrl}\"\\\\/;]", "").strip();
        int ponto = nome.lastIndexOf('.');
        String base = ponto >= 0 ? nome.substring(0, ponto) : nome;
        base = base.replaceAll("^[.\\s]+|[.\\s]+$", "");
        if (base.length() > MAX_NOME)
            base = base.substring(0, MAX_NOME).strip();
        if (base.isEmpty())
            base = NOME_PADRAO;
        return base + "." + tipo.getExtensao();
    }

    // inline (exibir) ou attachment (baixar). filename em ASCII para navegadores antigos e filename* em
    // UTF-8 (RFC 6266/5987) para manter os acentos.
    public static String contentDisposition(String nome, boolean download) {
        return (download ? "attachment" : "inline")
                + "; filename=\"" + nomeAscii(nome) + "\"; filename*=UTF-8''" + codificarRfc5987(nome);
    }

    // "850 bytes", "512 KB", "1,5 MB"
    public static String tamanhoLegivel(long bytes) {
        if (bytes < 1024)
            return bytes + " bytes";
        if (bytes < 1024 * 1024)
            return Math.round(bytes / 1024.0) + " KB";
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static String nomeAscii(String nome) {
        String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.replaceAll("[^\\x20-\\x7E]|[\"\\\\]", "_");
    }

    // attr-char da RFC 5987: letras, digitos e !#$&+-.^_`|~ passam; o resto vira %XX dos bytes UTF-8
    private static String codificarRfc5987(String nome) {
        StringBuilder resultado = new StringBuilder();
        for (byte b : nome.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || "!#$&+-.^_`|~".indexOf(c) >= 0)
                resultado.append(c);
            else
                resultado.append('%').append(String.format("%02X", b & 0xFF));
        }
        return resultado.toString();
    }

    // vazio quando o conteudo nao e JPEG, PNG nem WebP (GIF, SVG, PDF, texto, arquivo curto...)
    public static Optional<TipoImagem> detectar(byte[] conteudo) {
        if (conteudo == null)
            return Optional.empty();
        if (comecaCom(conteudo, 0, ASSINATURA_JPEG))
            return Optional.of(TipoImagem.JPEG);
        if (comecaCom(conteudo, 0, ASSINATURA_PNG))
            return Optional.of(TipoImagem.PNG);
        if (comecaCom(conteudo, 0, RIFF) && comecaCom(conteudo, 8, WEBP))
            return Optional.of(TipoImagem.WEBP);
        return Optional.empty();
    }

    private static boolean comecaCom(byte[] conteudo, int inicio, byte[] assinatura) {
        if (conteudo.length < inicio + assinatura.length)
            return false;
        for (int i = 0; i < assinatura.length; i++) {
            if (conteudo[inicio + i] != assinatura[i])
                return false;
        }
        return true;
    }
}
