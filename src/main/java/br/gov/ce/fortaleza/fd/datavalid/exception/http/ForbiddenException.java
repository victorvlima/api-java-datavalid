package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 403 - Não autorizado.
 * Problemas durante a autorização.
 */
public class ForbiddenException extends DataValidException {
    
    public ForbiddenException(String message) {
        super(403, message);
    }
    
    public ForbiddenException() {
        super(403, "Não autorizado - Problemas durante a autorização.");
    }
}
