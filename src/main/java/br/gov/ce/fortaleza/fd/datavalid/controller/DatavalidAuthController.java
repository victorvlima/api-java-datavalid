package br.gov.ce.fortaleza.fd.datavalid.controller;

import br.gov.ce.fortaleza.fd.datavalid.model.DatavalidAuth;
import br.gov.ce.fortaleza.fd.datavalid.service.DatavalidTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for DataValid Authentication
 * 
 * Provides endpoints to demonstrate and test DataValid API authentication flow
 * 
 * Endpoints:
 * - POST /api/datavalid/auth/token - Obtain a new access token
 * - GET /api/datavalid/auth/validate - Check if current token is valid
 * - POST /api/datavalid/auth/refresh - Refresh the access token
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@RestController
@RequestMapping("/api/datavalid/auth")
public class DatavalidAuthController {

    private final DatavalidTokenService tokenService;

    public DatavalidAuthController(DatavalidTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Obtain a new access token for DataValid API
     * 
     * @return ResponseEntity containing the token information
     * 
     * @apiNote Uses credentials configured in application.yaml or environment variables
     * Environment Variables:
     *   - DATAVALID_CONSUMER_KEY: API Consumer Key
     *   - DATAVALID_CONSUMER_SECRET: API Consumer Secret
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> obtainToken() {
        try {
            DatavalidAuth.TokenResponse tokenResponse = tokenService.obtainTokenResponse();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token obtained successfully");
            response.put("access_token", tokenResponse.getAccessToken());
            response.put("token_type", tokenResponse.getTokenType());
            response.put("expires_in", tokenResponse.getExpiresIn());
            response.put("scope", tokenResponse.getScope());
            
            return ResponseEntity.ok(response);
        } catch (DatavalidAuth.TokenAcquisitionException e) {
            return ResponseEntity.internalServerError().body(createErrorResponse(e));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e));
        }
    }

    /**
     * Validate if DataValid service is properly configured
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConfiguration() {
        Map<String, Object> response = new HashMap<>();
        response.put("isConfigured", tokenService.isConfigured());
        
        if (tokenService.isConfigured()) {
            response.put("message", "DataValid service is properly configured");
            response.put("status", "READY");
        } else {
            response.put("message", "DataValid credentials are not configured");
            response.put("status", "UNCONFIGURED");
            response.put("requiredConfig", new String[]{
                "DATAVALID_CONSUMER_KEY",
                "DATAVALID_CONSUMER_SECRET"
            });
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh the current access token
     * 
     * @return ResponseEntity containing the new token information
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken() {
        try {
            String newToken = tokenService.refreshToken();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token refreshed successfully");
            response.put("access_token", newToken);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse(e));
        }
    }

    /**
     * Helper method to create standardized error responses
     */
    private Map<String, Object> createErrorResponse(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", e.getClass().getSimpleName());
        error.put("message", e.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
