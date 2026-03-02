package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 500 - Erro no servidor.
 * Ocorreu algum erro interno no Servidor.
 */
public class InternalServerErrorException extends DataValidException {
    
    public InternalServerErrorException(String message) {
        super(500, message);
    }
    
    public InternalServerErrorException() {
        super(500, "Erro interno no servidor SERPRO.");
    }
}
