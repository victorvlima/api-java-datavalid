package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;

/**
 * Exceção para erros HTTP 413 - Request Entity Too Large.
 * A requisição tem um tamanho muito grande. O limite total é de 3MB.
 */
public class RequestTooLargeException extends DataValidException {
    
    public RequestTooLargeException(String message) {
        super(413, message);
    }
    
    public RequestTooLargeException() {
        super(413, "Requisição muito grande - O limite total para o tamanho da requisição é de 3MB.");
    }
}
