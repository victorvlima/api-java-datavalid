package br.gov.ce.fortaleza.fd.datavalid.service;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPf;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Service for facial biometry validation
 * Handles CPF and facial photo validation against government databases
 * 
 * API Endpoint: POST /v4/pf-facial
 * 
 * Image Requirements:
 * - Format: JPG, PNG, or PDF
 * - Minimum resolution: 250x250 pixels (face area)
 * - Recommended resolution: 750x750 pixels  
 * - Maximum file size: Must keep total request under 3MB
 * - Quality Standards: Follow ICAO standards
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@Service
public class FacialValidationService {

    private static final String API_BASE_URL = "https://gateway.apiserpro.serpro.gov.br/datavalid-demonstracao";
    private static final String FACIAL_ENDPOINT = "/v4/pf-facial";
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.85; // 85% similarity

    private final DatavalidTokenService tokenService;
    private final RestClient restClient;

    public FacialValidationService(
            DatavalidTokenService tokenService,
            RestClient.Builder restClientBuilder) {
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.build();
    }

    /**
     * Validate CPF and facial photo
     * 
     * @param cpf CPF number (11 digits)
     * @param photoBase64 Face image in base64 format
     * @return FacialPfResponse with validation results
     * @throws IllegalArgumentException if CPF or photo is invalid
     * @throws RestClientException if API request fails
     */
    public FacialPfResponse validateFacialWithCpf(String cpf, String photoBase64) {
        return validateFacialWithCpf(cpf, photoBase64, null);
    }

    /**
     * Validate CPF and facial photo with additional validation data
     * 
     * @param cpf CPF number (11 digits)
     * @param photoBase64 Face image in base64 format
     * @param validationData Optional data for cross-validation (name, birthdate, etc.)
     * @return FacialPfResponse with validation results
     * @throws IllegalArgumentException if CPF or photo is invalid
     * @throws RestClientException if API request fails
     */
    public FacialPfResponse validateFacialWithCpf(
            String cpf, 
            String photoBase64, 
            FacialPf.ValidationData validationData) {
        
        // Validate inputs
        validateInputs(cpf, photoBase64);

        try {
            // Build request
            FacialPf facialRequest = new FacialPf(
                    cpf,
                    new FacialPf.FacialData(photoBase64),
                    validationData
            );

            // Get valid token
            String token = tokenService.getToken();

            // Make API request
            FacialPfResponse response = restClient.post()
                    .uri(API_BASE_URL + FACIAL_ENDPOINT)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(facialRequest)
                    .retrieve()
                    .body(FacialPfResponse.class);

            if (response == null) {
                throw new RestClientException("Invalid response from DataValid API");
            }

            return response;

        } catch (Exception e) {
            throw new RestClientException("Facial validation request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate CPF and facial photo from file
     * 
     * @param cpf CPF number (11 digits)
     * @param photoFile Image file (JPG, PNG, or PDF)
     * @return FacialPfResponse with validation results
     * @throws IllegalArgumentException if inputs are invalid
     * @throws IOException if file cannot be read
     * @throws RestClientException if API request fails
     */
    public FacialPfResponse validateFacialWithFile(String cpf, File photoFile) throws IOException {
        return validateFacialWithFile(cpf, photoFile, null);
    }

    /**
     * Validate CPF and facial photo from file with additional validation data
     * 
     * @param cpf CPF number (11 digits)
     * @param photoFile Image file (JPG, PNG, or PDF)
     * @param validationData Optional data for cross-validation
     * @return FacialPfResponse with validation results
     * @throws IllegalArgumentException if inputs are invalid
     * @throws IOException if file cannot be read
     * @throws RestClientException if API request fails
     */
    public FacialPfResponse validateFacialWithFile(
            String cpf, 
            File photoFile,
            FacialPf.ValidationData validationData) throws IOException {
        
        // Validate file
        validatePhotoFile(photoFile);

        // Read file and convert to base64
        byte[] photoBytes = Files.readAllBytes(photoFile.toPath());
        String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);

        return validateFacialWithCpf(cpf, photoBase64, validationData);
    }

    /**
     * Check if facial validation result meets acceptance threshold
     * 
     * @param response Facial validation response
     * @param threshold Minimum similarity required (0.0 to 1.0)
     * @return true if facial similarity >= threshold, false otherwise
     */
    public boolean validateSimilarityThreshold(FacialPfResponse response, Double threshold) {
        if (response == null || response.getFoto() == null) {
            return false;
        }
        return response.getFoto().meetsThreshold(threshold);
    }

    /**
     * Check if facial validation result meets default acceptance threshold (85%)
     * 
     * @param response Facial validation response
     * @return true if facial similarity >= 85%, false otherwise
     */
    public boolean validateSimilarityThreshold(FacialPfResponse response) {
        return validateSimilarityThreshold(response, DEFAULT_SIMILARITY_THRESHOLD);
    }

    /**
     * Get similarity percentage from response
     * 
     * @param response Facial validation response
     * @return Similarity percentage (0-100) or null if not available
     */
    public Double getSimilarityPercentage(FacialPfResponse response) {
        if (response == null || response.getFoto() == null) {
            return null;
        }
        return response.getFoto().getSimilarityPercentage();
    }

    /**
     * Validate inputs for facial validation
     * 
     * @param cpf CPF number
     * @param photoBase64 Photo in base64 format
     * @throws IllegalArgumentException if validation fails
     */
    private void validateInputs(String cpf, String photoBase64) {
        if (cpf == null || cpf.isEmpty()) {
            throw new IllegalArgumentException("CPF cannot be null or empty");
        }

        // Remove non-numeric characters for validation
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        if (cleanCpf.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits. Provided: " + cpf);
        }

        if (photoBase64 == null || photoBase64.isEmpty()) {
            throw new IllegalArgumentException("Photo base64 cannot be null or empty");
        }

        if (photoBase64.length() > 4_000_000) { // Rough estimate for 3MB base64
            throw new IllegalArgumentException("Photo size exceeds maximum allowed (3MB)");
        }
    }

    /**
     * Validate photo file before reading
     * 
     * @param photoFile Photo file to validate
     * @throws IllegalArgumentException if file is invalid
     * @throws IOException if file cannot be read
     */
    private void validatePhotoFile(File photoFile) throws IOException {
        if (photoFile == null || !photoFile.exists()) {
            throw new IllegalArgumentException("Photo file not found or null");
        }

        if (!photoFile.isFile()) {
            throw new IllegalArgumentException("Photo is not a valid file");
        }

        // Check file extension
        String filename = photoFile.getName().toLowerCase();
        if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg") 
                && !filename.endsWith(".png") && !filename.endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Invalid file format. Supported: JPG, PNG, PDF. Provided: " + filename
            );
        }

        // Check file size (3MB max)
        long fileSizeInBytes = Files.size(photoFile.toPath());
        long maxSizeInBytes = 3 * 1024 * 1024; // 3MB
        
        if (fileSizeInBytes > maxSizeInBytes) {
            throw new IllegalArgumentException(
                    "Photo file exceeds maximum size of 3MB. Size: " + 
                    (fileSizeInBytes / (1024 * 1024)) + "MB"
            );
        }

        // Check minimum resolution (rough check based on file size)
        if (fileSizeInBytes < 5000) { // Less than 5KB is likely too small
            throw new IllegalArgumentException(
                    "Photo file is too small. Minimum resolution: 250x250 pixels"
            );
        }
    }
}
