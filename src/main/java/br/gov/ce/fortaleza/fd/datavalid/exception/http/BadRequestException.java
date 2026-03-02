package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 400 - Requisição inválida.
 * A requisição não foi aceita pois existe alguma inconsistência em algum campo da requisição.
 */
public class BadRequestException extends DataValidException {
    
    public BadRequestException(String message) {
        super(400, message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
