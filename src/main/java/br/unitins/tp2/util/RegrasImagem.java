package br.unitins.tp2.util;

import java.util.Optional;

// Regras das imagens dos planos, sem banco e sem S3 (testaveis isoladamente).
// O tipo vem SEMPRE do conteudo do arquivo (magic bytes), nunca da extensao nem do Content-Type
// informados pelo cliente: um .txt renomeado para .png nao passa.
public final class RegrasImagem {

    private static final byte[] ASSINATURA_JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] ASSINATURA_PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
    // WebP: "RIFF" + 4 bytes de tamanho + "WEBP"
    private static final byte[] RIFF = { 'R', 'I', 'F', 'F' };
    private static final byte[] WEBP = { 'W', 'E', 'B', 'P' };

    private RegrasImagem() {
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
