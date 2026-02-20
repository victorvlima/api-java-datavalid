package br.gov.ce.fortaleza.fd.datavalid.controller;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPf;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import br.gov.ce.fortaleza.fd.datavalid.service.FacialValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for facial validation endpoints
 * 
 * Provides endpoints to validate CPF with facial biometry against government databases
 * 
 * Endpoints:
 * - POST /api/datavalid/facial/validate-with-base64 - Validate with base64 photo
 * - POST /api/datavalid/facial/validate-with-file - Validate with photo file
 * - GET /api/datavalid/facial/health - Check service health
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@RestController
@RequestMapping("/api/datavalid/facial")
public class FacialValidationController {

    private final FacialValidationService facialValidationService;

    public FacialValidationController(FacialValidationService facialValidationService) {
        this.facialValidationService = facialValidationService;
    }

    /**
     * Validate CPF and facial photo (base64)
     * 
     * @param request Facial validation request containing CPF and photo base64
     * @return Facial validation response with similarity score
     * 
     * Example request:
     * {
     *   "cpf": "12345678901",
     *   "foto": {
     *     "imagem": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
     *   }
     * }
     */
    @PostMapping("/validate-with-base64")
    public ResponseEntity<Map<String, Object>> validateWithBase64(
            @RequestBody FacialValidationRequest request) {
        try {
            // Validate request
            if (request == null || request.getCpf() == null || request.getFoto() == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("CPF and foto base64 are required"));
            }

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                    request.getCpf(),
                    request.getFoto().getImagem(),
                    request.getValidacao()
            );

            // Check similarity threshold
            Double similarity = facialValidationService.getSimilarityPercentage(response);
            boolean meetsThreshold = facialValidationService.validateSimilarityThreshold(response);

            // Build response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Facial validation completed");
            successResponse.put("cpf", request.getCpf());
            successResponse.put("fotoExiste", response.getFotoExiste());
            successResponse.put("similarity", similarity);
            successResponse.put("meetsThreshold", meetsThreshold);
            successResponse.put("details", response.getFoto());
            successResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(successResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Facial validation error: " + e.getMessage()));
        }
    }

    /**
     * Validate CPF and facial photo (file upload)
     * 
     * @param cpf CPF number (11 digits)
     * @param filePath Path to the facial photo file
     * @param validationData Optional validation data (name, birthdate, etc.)
     * @return Facial validation response with similarity score
     * 
     * Supported formats: JPG, PNG, PDF
     * Minimum resolution: 250x250 pixels
     * Maximum size: 3MB
     */
    @PostMapping("/validate-with-file")
    public ResponseEntity<Map<String, Object>> validateWithFile(
            @RequestParam String cpf,
            @RequestParam String filePath,
            @RequestBody(required = false) FacialPf.ValidationData validationData) {
        try {
            // Validate CPF
            if (cpf == null || cpf.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("CPF is required"));
            }

            // Load photo file
            File photoFile = new File(filePath);
            if (!photoFile.exists()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Photo file not found: " + filePath));
            }

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithFile(
                    cpf,
                    photoFile,
                    validationData
            );

            // Check similarity threshold
            Double similarity = facialValidationService.getSimilarityPercentage(response);
            boolean meetsThreshold = facialValidationService.validateSimilarityThreshold(response);

            // Build response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Facial validation completed");
            successResponse.put("cpf", cpf);
            successResponse.put("photoFile", filePath);
            successResponse.put("fotoExiste", response.getFotoExiste());
            successResponse.put("similarity", similarity);
            successResponse.put("meetsThreshold", meetsThreshold);
            successResponse.put("details", response.getFoto());
            successResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(successResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Facial validation error: " + e.getMessage()));
        }
    }

    /**
     * Check if facial validation service is healthy
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "FacialValidation");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoint", "/api/datavalid/facial");
        response.put("supportedFormats", new String[]{"JPG", "PNG", "PDF"});
        response.put("minResolution", "250x250 pixels");
        response.put("maxFileSize", "3MB");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    // ==================== Helper Classes ====================

    /**
     * Facial validation request DTO
     */
    public static class FacialValidationRequest {
        private String cpf;
        private FacialPf.FacialData foto;
        private FacialPf.ValidationData validacao;

        // Constructors
        public FacialValidationRequest() {}

        public FacialValidationRequest(String cpf, FacialPf.FacialData foto) {
            this.cpf = cpf;
            this.foto = foto;
        }

        // Getters and Setters
        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public FacialPf.FacialData getFoto() {
            return foto;
        }

        public void setFoto(FacialPf.FacialData foto) {
            this.foto = foto;
        }

        public FacialPf.ValidationData getValidacao() {
            return validacao;
        }

        public void setValidacao(FacialPf.ValidationData validacao) {
            this.validacao = validacao;
        }
    }
}
