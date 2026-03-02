package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 401 - Não autenticado.
 * Problemas durante a autenticação. Verifique seu par de chaves.
 */
public class UnauthorizedException extends DataValidException {
    
    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    public UnauthorizedException() {
        super(401, "Não autenticado - Problemas durante a autenticação. Verifique seu token/chaves.");
    }
}
