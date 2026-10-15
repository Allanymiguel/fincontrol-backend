package br.unitins.tp2.exception;

// Lancada quando uma API externa (ex.: ViaCEP) esta fora do ar ou nao respondeu a tempo
public class ServicoExternoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServicoExternoException(String message, Throwable cause) {
        super(message, cause);
    }
}
