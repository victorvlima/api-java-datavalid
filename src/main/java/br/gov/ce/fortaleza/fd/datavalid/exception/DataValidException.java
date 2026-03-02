package br.gov.ce.fortaleza.fd.datavalid.exception;

/**
 * Exceção base para erros da API SERPRO DataValid.
 * Todas as exceções específicas da API devem estender esta classe.
 */
public class DataValidException extends RuntimeException {
    
    private final int httpStatus;
    private final String errorCode;
    private final String link;

    public DataValidException(String message) {
        super(message);
        this.httpStatus = 0;
        this.errorCode = null;
        this.link = null;
    }

    public DataValidException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.errorCode = null;
        this.link = null;
    }

    public DataValidException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = null;
        this.link = null;
    }

    public DataValidException(int httpStatus, String errorCode, String message, String link) {
        super(String.format("[%s] %s - %s", errorCode, message, link));
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.link = link;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getLink() {
        return link;
    }
}
