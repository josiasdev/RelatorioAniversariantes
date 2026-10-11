package com.github.josiasdev.RelatorioAniversariantes.exception;

public class WhatsAppException extends RuntimeException {

    public WhatsAppException(String message) {
        super(message);
    }

    public WhatsAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
