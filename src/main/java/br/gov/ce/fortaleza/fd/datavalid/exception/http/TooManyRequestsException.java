package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 429 - Limite de requisições excedido.
 * O cliente fez muitas requisições em um curto período de tempo.
 * Respeite os limites de taxa (rate limit) da API.
 */
public class TooManyRequestsException extends DataValidException {
    
    public TooManyRequestsException(String message) {
        super(429, message);
    }
    
    public TooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
}
