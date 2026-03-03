package br.gov.ce.fortaleza.fd.datavalid.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.gov.ce.fortaleza.fd.datavalid.exception.http.BadGatewayException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.BadRequestException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.ForbiddenException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.GatewayTimeoutException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.InternalServerErrorException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.NotFoundException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.RequestTooLargeException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.ServiceUnavailableException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.TooManyRequestsException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.UnauthorizedException;
import br.gov.ce.fortaleza.fd.datavalid.exception.http.UnprocessableEntityException;

/**
 * Handler global para tratamento de exceções da aplicação.
 * Intercepta exceções do DataValid e retorna respostas HTTP adequadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Tratamento para BadRequestException (400)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Tratamento para UnauthorizedException (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    /**
     * Tratamento para ForbiddenException (403)
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex);
    }

    /**
     * Tratamento para NotFoundException (404)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex);
    }

    /**
     * Tratamento para RequestTooLargeException (413)
     */
    @ExceptionHandler(RequestTooLargeException.class)
    public ResponseEntity<Map<String, Object>> handleRequestTooLarge(RequestTooLargeException ex) {
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, ex);
    }

    /**
     * Tratamento para UnprocessableEntityException (422)
     */
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Map<String, Object>> handleUnprocessableEntity(UnprocessableEntityException ex) {
        // Log específico para erros 422
        LOGGER.warning(String.format(
            "UnprocessableEntityException capturada | ErrorCode DataValid: %s | Message: %s | Link: %s",
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getLink()
        ));
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 422);
        body.put("error", "Unprocessable Entity");
        body.put("message", ex.getMessage());
        
        // Adiciona o código de erro específico do DataValid
        if (ex.getErrorCode() != null) {
            body.put("code", ex.getErrorCode());
        }
        
        // Adiciona link para documentação se disponível
        if (ex.getLink() != null) {
            body.put("link", ex.getLink());
        }
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * Tratamento para TooManyRequestsException (429)
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(TooManyRequestsException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex);
    }

    /**
     * Tratamento para InternalServerErrorException (500)
     */
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(InternalServerErrorException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    /**
     * Tratamento para BadGatewayException (502)
     */
    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Map<String, Object>> handleBadGateway(BadGatewayException ex) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex);
    }

    /**
     * Tratamento para ServiceUnavailableException (503)
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(ServiceUnavailableException ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex);
    }

    /**
     * Tratamento para GatewayTimeoutException (504)
     */
    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayTimeout(GatewayTimeoutException ex) {
        return buildErrorResponse(HttpStatus.GATEWAY_TIMEOUT, ex);
    }

    /**
     * Tratamento genérico para DataValidException
     */
    @ExceptionHandler(DataValidException.class)
    public ResponseEntity<Map<String, Object>> handleDataValidException(DataValidException ex) {
        HttpStatus status = ex.getHttpStatus() > 0 
            ? HttpStatus.valueOf(ex.getHttpStatus()) 
            : HttpStatus.INTERNAL_SERVER_ERROR;
        return buildErrorResponse(status, ex);
    }

    /**
     * Tratamento para exceções genéricas não tratadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log de exceção não tratada
        LOGGER.severe(String.format(
            "Exceção não tratada: %s | Message: %s",
            ex.getClass().getName(),
            ex.getMessage()
        ));
        ex.printStackTrace();
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "Ocorreu um erro inesperado no servidor");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Método auxiliar para construir resposta de erro
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, DataValidException ex) {
        // Log detalhado para debug
        LOGGER.warning(String.format(
            "GlobalExceptionHandler capturou: %s | HTTP Status: %d | ErrorCode: %s | Message: %s",
            ex.getClass().getSimpleName(),
            status.value(),
            ex.getErrorCode(),
            ex.getMessage()
        ));
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        
        if (ex.getErrorCode() != null) {
            body.put("code", ex.getErrorCode());
        }
        
        if (ex.getLink() != null) {
            body.put("link", ex.getLink());
        }
        
        return ResponseEntity.status(status).body(body);
    }
}
