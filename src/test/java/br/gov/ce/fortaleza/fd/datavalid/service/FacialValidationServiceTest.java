package br.gov.ce.fortaleza.fd.datavalid.service;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPf;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FacialValidationService
 * 
 * Tests facial biometry validation functionality
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Facial Validation Service Tests")
class FacialValidationServiceTest {

    private FacialValidationService facialValidationService;

    @Mock
    private DatavalidTokenService tokenService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private static final String TEST_CPF = "25774435016";
    private static final String TEST_PHOTO_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String TEST_TOKEN = "test-token-12345";
    private static final double TEST_SIMILARITY = 0.92;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        
        when(tokenService.getToken()).thenReturn(TEST_TOKEN);
        
        facialValidationService = new FacialValidationService(tokenService, builder);
    }

    @Test
    @DisplayName("Should validate facial with CPF and base64 photo successfully")
    void testValidateFacialWithCpfBase64Success() {
        // Arrange
        FacialPfResponse expectedResponse = createMockResponse();
        setupRestClientMock(expectedResponse);

        // Act
        FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                TEST_CPF,
                TEST_PHOTO_BASE64
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.getFotoExiste());
        assertEquals(TEST_SIMILARITY, response.getFoto().getFaceSimilaridade());
        
        verify(tokenService).getToken();
        verify(restClient).post();
    }

    @Test
    @DisplayName("Should validate facial with validation data")
    void testValidateFacialWithValidationDataSuccess() {
        // Arrange
        FacialPfResponse expectedResponse = createMockResponse();
        setupRestClientMock(expectedResponse);

        FacialPf.ValidationData validationData = new FacialPf.ValidationData(
                "João Silva",
                "1990-01-15"
        );

        // Act
        FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                TEST_CPF,
                TEST_PHOTO_BASE64,
                validationData
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.getFotoExiste());
    }

    @Test
    @DisplayName("Should throw exception for null CPF")
    void testValidateFacialWithNullCpf() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithCpf(
                    null,
                    TEST_PHOTO_BASE64
            );
        });
    }

    @Test
    @DisplayName("Should throw exception for empty CPF")
    void testValidateFacialWithEmptyCpf() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithCpf(
                    "",
                    TEST_PHOTO_BASE64
            );
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid CPF length")
    void testValidateFacialWithInvalidCpfLength() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithCpf(
                    "12345",
                    TEST_PHOTO_BASE64
            );
        });
    }

    @Test
    @DisplayName("Should throw exception for null photo")
    void testValidateFacialWithNullPhoto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithCpf(
                    TEST_CPF,
                    null
            );
        });
    }

    @Test
    @DisplayName("Should throw exception for empty photo")
    void testValidateFacialWithEmptyPhoto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithCpf(
                    TEST_CPF,
                    ""
            );
        });
    }

    @Test
    @DisplayName("Should validate similarity threshold - meets threshold")
    void testValidateSimilarityThresholdMeets() {
        // Arrange
        FacialPfResponse response = createMockResponse();

        // Act
        boolean result = facialValidationService.validateSimilarityThreshold(
                response,
                0.85
        );

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should validate similarity threshold - below threshold")
    void testValidateSimilarityThresholdBelowThreshold() {
        // Arrange
        FacialPfResponse response = createMockResponse();

        // Act
        boolean result = facialValidationService.validateSimilarityThreshold(
                response,
                0.95 // Higher than actual similarity
        );

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should validate default similarity threshold")
    void testValidateDefaultThreshold() {
        // Arrange
        FacialPfResponse response = createMockResponse();

        // Act
        boolean result = facialValidationService.validateSimilarityThreshold(response);

        // Assert
        assertTrue(result); // 0.92 is > 0.85 (default)
    }

    @Test
    @DisplayName("Should get similarity percentage")
    void testGetSimilarityPercentage() {
        // Arrange
        FacialPfResponse response = createMockResponse();

        // Act
        Double percentage = facialValidationService.getSimilarityPercentage(response);

        // Assert
        assertNotNull(percentage);
        assertEquals(92.0, percentage);
    }

    @Test
    @DisplayName("Should return null for null response similarity")
    void testGetSimilarityPercentageNullResponse() {
        // Act
        Double percentage = facialValidationService.getSimilarityPercentage(null);

        // Assert
        assertNull(percentage);
    }

    @Test
    @DisplayName("Should validate facial from file successfully")
    void testValidateFacialWithFileSuccess() throws IOException {
        // Arrange
        FacialPfResponse expectedResponse = createMockResponse();
        setupRestClientMock(expectedResponse);

        // Create temporary test file
        Path tempFile = Files.createTempFile("test_photo", ".jpg");
        Files.write(tempFile, new byte[]{-1, -40, -1, -32}); // JPEG header
        
        try {
            // Act
            FacialPfResponse response = facialValidationService.validateFacialWithFile(
                    TEST_CPF,
                    tempFile.toFile()
            );

            // Assert
            assertNotNull(response);
            assertTrue(response.getFotoExiste());

        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void testValidateFacialWithNonExistentFile() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            facialValidationService.validateFacialWithFile(
                    TEST_CPF,
                    new File("/non/existent/file.jpg")
            );
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid file format")
    void testValidateFacialWithInvalidFileFormat() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("test_photo", ".txt");
        
        try {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                facialValidationService.validateFacialWithFile(
                        TEST_CPF,
                        tempFile.toFile()
                );
            });

        } finally {
            Files.delete(tempFile);
        }
    }

    // Helper methods

    private FacialPfResponse createMockResponse() {
        FacialPfResponse response = new FacialPfResponse();
        response.setFotoExiste(true);
        
        FacialPfResponse.FaceValidationResult faceResult = new FacialPfResponse.FaceValidationResult();
        faceResult.setFaceSimilaridade(TEST_SIMILARITY);
        response.setFoto(faceResult);
        
        return response;
    }

    private void setupRestClientMock(FacialPfResponse expectedResponse) {
        when(restClient.post()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.body(any(Object.class))).thenReturn((RestClient.RequestBodySpec) requestHeadersSpec);
        when(((RestClient.RequestBodySpec) requestHeadersSpec).retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(FacialPfResponse.class)).thenReturn(expectedResponse);
    }
}
