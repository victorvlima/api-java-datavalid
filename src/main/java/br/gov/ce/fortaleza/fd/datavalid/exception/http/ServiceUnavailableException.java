package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 503 - Serviço Indisponível.
 * Erro interno do servidor. Algum serviço integrado ao Datavalid está indisponível no momento.
 */
public class ServiceUnavailableException extends DataValidException {
    
    public ServiceUnavailableException(String message) {
        super(503, message);
    }
    
    public ServiceUnavailableException() {
        super(503, "Serviço indisponível - Algum serviço integrado ao Datavalid está indisponível no momento.");
    }
}
