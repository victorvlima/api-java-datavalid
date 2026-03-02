package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 504 - Gateway Timeout.
 * Ocorreu algum erro de rede e o gateway não respondeu a tempo.
 */
public class GatewayTimeoutException extends DataValidException {
    
    public GatewayTimeoutException(String message) {
        super(504, message);
    }
    
    public GatewayTimeoutException() {
        super(504, "Gateway Timeout - Erro de rede, o gateway não respondeu a tempo.");
    }
}
