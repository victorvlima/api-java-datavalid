package br.gov.ce.fortaleza.fd.datavalid.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatavalidAuth class
 * 
 * Tests OAuth2 client credentials flow for SERPRO DataValid API authentication
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataValid Authentication Tests")
class DatavalidAuthTest {

    private DatavalidAuth datavalidAuth;
    
    @Mock
    private RestClient restClient;
    
    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    
    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;
    
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private static final String TEST_CONSUMER_KEY = "testKey123456789012345678901234";
    private static final String TEST_CONSUMER_SECRET = "testSecret1234567890123456789012";
    private static final String TEST_ACCESS_TOKEN = "eyJ4NXQiO<<TOKEN_DE_1500_CARACTERES>>YzjB1wbrqHzqa4O1Qo-3DnQKkZhE5bvzM-lJHTbxnX6NRYsJ8ehrQ";
    private static final long TEST_EXPIRES_IN = 3600L;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        datavalidAuth = new DatavalidAuth(builder);
    }

    @Test
    @DisplayName("Should obtain token with client credentials")
    void testObtainTokenSuccess() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Act
        DatavalidAuth.TokenResponse response = datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(TEST_EXPIRES_IN, response.getExpiresIn());
        assertEquals("default", response.getScope());
        
        verifyRestClientCalls();
    }

    @Test
    @DisplayName("Should use demo environment when requested")
    void testObtainTokenDemoEnvironment() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Act
        DatavalidAuth.TokenResponse response = datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, true);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
    }

    @Test
    @DisplayName("Should encode credentials in Base64")
    void testCredentialsEncoding() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Expected Base64 encoded credentials
        String expectedEncoded = Base64.getEncoder().encodeToString(
                (TEST_CONSUMER_KEY + ":" + TEST_CONSUMER_SECRET).getBytes(StandardCharsets.UTF_8)
        );

        // Act
        datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);

        // Assert - Verify the authorization header was set correctly
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(requestHeadersSpec).header(eq(HttpHeaders.AUTHORIZATION), captor.capture());
        
        String authHeader = captor.getValue();
        assertTrue(authHeader.startsWith("Basic "), "Authorization header should start with 'Basic '");
        assertTrue(authHeader.contains(expectedEncoded) || verifyBase64Matching(authHeader, expectedEncoded));
    }

    @Test
    @DisplayName("Should cache token for subsequent calls")
    void testTokenCaching() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Act - First call
        DatavalidAuth.TokenResponse response1 = datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);
        String cachedToken = datavalidAuth.getCachedToken();

        // Assert
        assertNotNull(response1);
        assertEquals(TEST_ACCESS_TOKEN, cachedToken);
        assertEquals(response1.getAccessToken(), cachedToken);
    }

    @Test
    @DisplayName("Should throw exception on token acquisition failure")
    void testTokenAcquisitionFailure() {
        // Arrange
        when(restClient.post()).thenThrow(
                new RestClientException("Connection refused")
        );

        // Act & Assert
        assertThrows(DatavalidAuth.TokenAcquisitionException.class, () -> {
            datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);
        });
    }

    @Test
    @DisplayName("Should return valid cached token if not expired")
    void testGetValidTokenWithCache() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Act - First call obtains token
        datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);
        
        // Act - Get valid token should return cached
        String token = datavalidAuth.getValidToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);

        // Assert
        assertEquals(TEST_ACCESS_TOKEN, token);
        // Verify restClient was called only once (for the obtainToken call)
        verify(restClient, times(1)).post();
    }

    @Test
    @DisplayName("Should clear cached token")
    void testClearCache() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);
        datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);

        // Assert token is cached
        assertNotNull(datavalidAuth.getCachedToken());

        // Act
        datavalidAuth.clearCache();

        // Assert
        assertNull(datavalidAuth.getCachedToken());
    }

    @Test
    @DisplayName("Should set correct HTTP headers")
    void testHttpHeaders() {
        // Arrange
        DatavalidAuth.TokenResponse expectedResponse = new DatavalidAuth.TokenResponse(
                TEST_ACCESS_TOKEN,
                "Bearer",
                TEST_EXPIRES_IN,
                "default"
        );

        setupRestClientMock(expectedResponse);

        // Act
        datavalidAuth.obtainToken(TEST_CONSUMER_KEY, TEST_CONSUMER_SECRET, false);

        // Assert
        verify(requestHeadersSpec).header(
                eq(HttpHeaders.CONTENT_TYPE),
                eq(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        );
    }

    // Helper Methods

    private void setupRestClientMock(DatavalidAuth.TokenResponse expectedResponse) {
        when(restClient.post()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.body(anyString())).thenReturn((RestClient.RequestBodySpec) requestHeadersSpec);
        when(((RestClient.RequestBodySpec) requestHeadersSpec).retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(DatavalidAuth.TokenResponse.class)).thenReturn(expectedResponse);
    }

    private void verifyRestClientCalls() {
        verify(restClient).post();
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec, times(2)).header(anyString(), anyString());
    }

    private boolean verifyBase64Matching(String authHeader, String expected) {
        return authHeader.equals("Basic " + expected);
    }
}
