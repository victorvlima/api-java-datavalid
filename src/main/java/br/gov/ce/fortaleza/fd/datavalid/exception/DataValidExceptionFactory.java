package br.gov.ce.fortaleza.fd.datavalid.exception;

import br.gov.ce.fortaleza.fd.datavalid.exception.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory para criar exceções apropriadas baseadas na resposta da API SERPRO DataValid.
 */
public class DataValidExceptionFactory {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Cria uma exceção apropriada baseada no status HTTP e corpo da resposta.
     * 
     * @param statusCode código de status HTTP
     * @param responseBody corpo da resposta (pode ser JSON com code/link)
     * @return exceção apropriada
     */
    public static DataValidException createException(int statusCode, String responseBody) {
        // Tenta extrair code e link do JSON de erro
        String errorCode = null;
        String errorLink = null;
        
        try {
            JsonNode jsonNode = mapper.readTree(responseBody);
            if (jsonNode.has("code")) {
                errorCode = jsonNode.get("code").asText();
            }
            if (jsonNode.has("link")) {
                errorLink = jsonNode.get("link").asText();
            }
        } catch (Exception e) {
            // Se não for JSON válido, ignora e usa a resposta como está
        }
        
        return createException(statusCode, errorCode, responseBody, errorLink);
    }
    
    /**
     * Cria uma exceção apropriada baseada nos parâmetros fornecidos.
     * 
     * @param statusCode código de status HTTP
     * @param errorCode código de erro (ex: "DV042")
     * @param message mensagem de erro
     * @param link link para documentação
     * @return exceção apropriada
     */
    public static DataValidException createException(int statusCode, String errorCode, String message, String link) {
        switch (statusCode) {
            case 400:
                return new BadRequestException(message);
            
            case 401:
                return new UnauthorizedException(message);
            
            case 403:
                return new ForbiddenException(message);
            
            case 404:
                return new NotFoundException(message);
            
            case 413:
                return new RequestTooLargeException(message);
            
            case 422:
                if (errorCode != null && !errorCode.isEmpty()) {
                    return new UnprocessableEntityException(errorCode, message, link);
                }
                return new UnprocessableEntityException("UNKNOWN", message, link);
            
            case 500:
                return new InternalServerErrorException(message);
            
            case 502:
                return new BadGatewayException(message);
            
            case 503:
                return new ServiceUnavailableException(message);
            
            case 504:
                return new GatewayTimeoutException(message);
            
            default:
                return new DataValidException(statusCode, message);
        }
    }
    
    /**
     * Verifica se o status HTTP indica sucesso (2xx).
     * 
     * @param statusCode código de status HTTP
     * @return true se for sucesso
     */
    public static boolean isSuccessStatus(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Verifica se o status HTTP indica erro do cliente (4xx).
     * 
     * @param statusCode código de status HTTP
     * @return true se for erro do cliente
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Verifica se o status HTTP indica erro do servidor (5xx).
     * 
     * @param statusCode código de status HTTP
     * @return true se for erro do servidor
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
}
