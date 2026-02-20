package br.gov.ce.fortaleza.fd.datavalid.service;

import br.gov.ce.fortaleza.fd.datavalid.model.DatavalidAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing DataValid API authentication tokens
 * 
 * This service encapsulates token management logic and provides a convenient
 * interface for obtaining and maintaining authentication tokens to consume
 * the DataValid API.
 * 
 * Configuration:
 * - datavalid.consumer.key: API Consumer Key from SERPRO Client Area
 * - datavalid.consumer.secret: API Consumer Secret from SERPRO Client Area
 * - datavalid.use.demo: Whether to use demonstration environment (default: false)
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@Service
public class DatavalidTokenService {

    private final DatavalidAuth datavalidAuth;

    @Value("${datavalid.consumer.key:}")
    private String consumerKey;

    @Value("${datavalid.consumer.secret:}")
    private String consumerSecret;

    @Value("${datavalid.use.demo:false}")
    private boolean useDemo;

    public DatavalidTokenService(DatavalidAuth datavalidAuth) {
        this.datavalidAuth = datavalidAuth;
    }

    /**
     * Get a valid authentication token
     * Returns cached token if still valid, otherwise requests a new one
     *
     * @return Valid access token for DataValid API
     * @throws DatavalidAuth.TokenAcquisitionException if token acquisition fails
     */
    public String getToken() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "DataValid credentials not configured. " +
                    "Set datavalid.consumer.key and datavalid.consumer.secret properties."
            );
        }
        return datavalidAuth.getValidToken(consumerKey, consumerSecret, useDemo);
    }

    /**
     * Refresh the authentication token (useful for forced renewal)
     *
     * @return Fresh access token
     * @throws DatavalidAuth.TokenAcquisitionException if token acquisition fails
     */
    public String refreshToken() {
        datavalidAuth.clearCache();
        return datavalidAuth.obtainToken(consumerKey, consumerSecret, useDemo).getAccessToken();
    }

    /**
     * Get detailed token response including metadata
     *
     * @return TokenResponse with access_token, token_type, expires_in, and scope
     * @throws DatavalidAuth.TokenAcquisitionException if token acquisition fails
     */
    public DatavalidAuth.TokenResponse obtainTokenResponse() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "DataValid credentials not configured. " +
                    "Set datavalid.consumer.key and datavalid.consumer.secret properties."
            );
        }
        return datavalidAuth.obtainToken(consumerKey, consumerSecret, useDemo);
    }

    /**
     * Check if DataValid credentials are configured
     *
     * @return true if both consumer key and secret are set, false otherwise
     */
    public boolean isConfigured() {
        return consumerKey != null && !consumerKey.isEmpty() &&
               consumerSecret != null && !consumerSecret.isEmpty();
    }

    /**
     * Clear cached token
     */
    public void clearTokenCache() {
        datavalidAuth.clearCache();
    }
}
