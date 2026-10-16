package br.unitins.tp2.exception;

// Login recusado (credenciais erradas, conta desativada, sessao expirada) ou requisicao sem usuario logado
public class NaoAutenticadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NaoAutenticadoException(String message) {
        super(message);
    }
}
