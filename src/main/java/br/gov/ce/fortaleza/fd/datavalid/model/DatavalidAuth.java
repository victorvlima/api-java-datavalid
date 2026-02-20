package br.gov.ce.fortaleza.fd.datavalid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * DataValid authentication manager for OAuth2 token handling
 * Implements the client credentials flow for SERPRO DataValid API authentication
 *
 * @author Design Pattern: Service Component
 * @version 1.0
 */
@Component
public class DatavalidAuth {

    private static final String TOKEN_ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String DEMO_TOKEN_ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/datavalid-demonstracao/token";
    private static final long TOKEN_REFRESH_MARGIN = 300; // 5 minutes buffer

    private final RestClient restClient;
    private String cachedToken;
    private long tokenExpirationTime;

    public DatavalidAuth(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Request an access token using client credentials
     * 
     * @param consumerKey The API consumer key from SERPRO Client Area
     * @param consumerSecret The API consumer secret from SERPRO Client Area
     * @param isDemo Whether to use the demonstration environment
     * @return TokenResponse object containing the access token and metadata
     * @throws RestClientException if the token request fails
     */
    public TokenResponse obtainToken(String consumerKey, String consumerSecret, boolean isDemo) {
        try {
            String credentials = consumerKey + ":" + consumerSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

            String endpoint = isDemo ? DEMO_TOKEN_ENDPOINT : TOKEN_ENDPOINT;

            TokenResponse response = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body("grant_type=" + GRANT_TYPE)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response != null) {
                this.cachedToken = response.getAccessToken();
                // Calculate expiration time considering a 5-minute safety margin
                this.tokenExpirationTime = System.currentTimeMillis() 
                        + ((response.getExpiresIn() - TOKEN_REFRESH_MARGIN) * 1000);
            }

            return response;
        } catch (RestClientException e) {
            throw new TokenAcquisitionException(
                    "Failed to obtain token from SERPRO DataValid API", e
            );
        }
    }

    /**
     * Request an access token using client credentials (production environment)
     *
     * @param consumerKey The API consumer key from SERPRO Client Area
     * @param consumerSecret The API consumer secret from SERPRO Client Area
     * @return TokenResponse object containing the access token and metadata
     * @throws RestClientException if the token request fails
     */
    public TokenResponse obtainToken(String consumerKey, String consumerSecret) {
        return obtainToken(consumerKey, consumerSecret, false);
    }

    /**
     * Get the cached token if it's still valid, otherwise request a new one
     *
     * @param consumerKey The API consumer key
     * @param consumerSecret The API consumer secret
     * @param isDemo Whether to use the demonstration environment
     * @return Valid access token
     */
    public String getValidToken(String consumerKey, String consumerSecret, boolean isDemo) {
        if (isTokenValid()) {
            return cachedToken;
        }
        TokenResponse response = obtainToken(consumerKey, consumerSecret, isDemo);
        return response.getAccessToken();
    }

    /**
     * Check if the cached token is still valid
     *
     * @return true if token exists and hasn't expired, false otherwise
     */
    private boolean isTokenValid() {
        return cachedToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }

    /**
     * Get the current cached token
     *
     * @return The cached access token or null if not available
     */
    public String getCachedToken() {
        return cachedToken;
    }

    /**
     * Clear the cached token (useful for logout or token refresh)
     */
    public void clearCache() {
        this.cachedToken = null;
        this.tokenExpirationTime = 0;
    }

    /**
     * Inner class representing the OAuth2 token response from SERPRO DataValid API
     * Maps to the JSON response from the /token endpoint
     */
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private Long expiresIn;

        @JsonProperty("scope")
        private String scope;

        // Constructors
        public TokenResponse() {}

        public TokenResponse(String accessToken, String tokenType, Long expiresIn, String scope) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.scope = scope;
        }

        // Getters and Setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(50, accessToken.length())) + "..." : null) + '\'' +
                    ", tokenType='" + tokenType + '\'' +
                    ", expiresIn=" + expiresIn +
                    ", scope='" + scope + '\'' +
                    '}';
        }
    }

    /**
     * Custom exception for token acquisition failures
     */
    public static class TokenAcquisitionException extends RuntimeException {
        public TokenAcquisitionException(String message) {
            super(message);
        }

        public TokenAcquisitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
