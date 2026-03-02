package br.gov.ce.fortaleza.fd.datavalid.exception.http;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;
import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidErrorCode;

/**
 * Exceção para erros HTTP 422 - Requisição não processada.
 * A requisição não pode ser processada porque algum valor inválido foi enviado.
 */
public class UnprocessableEntityException extends DataValidException {
    
    private final DataValidErrorCode errorCode;
    
    public UnprocessableEntityException(String code, String message, String link) {
        super(422, code, message, link);
        this.errorCode = DataValidErrorCode.fromCode(code);
    }
    
    public UnprocessableEntityException(DataValidErrorCode errorCode, String link) {
        super(422, errorCode.name(), errorCode.getDescription(), link);
        this.errorCode = errorCode;
    }
    
    public DataValidErrorCode getErrorCodeEnum() {
        return errorCode;
    }
}
