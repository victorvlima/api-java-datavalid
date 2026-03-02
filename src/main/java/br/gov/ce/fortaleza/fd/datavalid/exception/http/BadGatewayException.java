package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 502 - Bad Gateway.
 * Problema entre o gateway e o backend do Datavalid. Tente novamente.
 */
public class BadGatewayException extends DataValidException {
    
    public BadGatewayException(String message) {
        super(502, message);
    }
    
    public BadGatewayException() {
        super(502, "Bad Gateway - Problema entre o gateway e o backend do Datavalid. Tente novamente.");
    }
}
