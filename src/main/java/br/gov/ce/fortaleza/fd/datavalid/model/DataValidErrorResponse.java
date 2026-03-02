package br.gov.ce.fortaleza.fd.datavalid.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo para capturar respostas de erro da API SERPRO DataValid.
 * Usado quando a API retorna códigos HTTP de erro (4xx, 5xx).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataValidErrorResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("link")
    private String link;
    
    @JsonProperty("message")
    private String message;
    
    public DataValidErrorResponse() {
    }
    
    public DataValidErrorResponse(String code, String link, String message) {
        this.code = code;
        this.link = link;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return String.format("DataValidErrorResponse{code='%s', message='%s', link='%s'}", 
                             code, message, link);
    }
}
