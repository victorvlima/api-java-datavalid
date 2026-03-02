package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 404 - Não encontrado.
 * Verifique se há alguma inconsistência na URL, e se é válido para a API/versão utilizada.
 */
public class NotFoundException extends DataValidException {
    
    public NotFoundException(String message) {
        super(404, message);
    }
    
    public NotFoundException() {
        super(404, "Recurso não encontrado - Verifique a URL e a versão da API.");
    }
}
