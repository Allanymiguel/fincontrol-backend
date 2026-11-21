package br.unitins.tp2.util;

// Formatos aceitos nas imagens dos planos. Nao vai para o banco como enum (la fica o Content-Type),
// por isso nao implementa EnumComId nem tem converter.
public enum TipoImagem {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extensao;

    TipoImagem(String contentType, String extensao) {
        this.contentType = contentType;
        this.extensao = extensao;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtensao() {
        return extensao;
    }
}
