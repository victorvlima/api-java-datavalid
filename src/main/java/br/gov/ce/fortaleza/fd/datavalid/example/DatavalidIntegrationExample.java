package br.gov.ce.fortaleza.fd.datavalid.example;

import br.gov.ce.fortaleza.fd.datavalid.service.DatavalidTokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Example integration with DataValid API
 * Demonstrates complete authentication and API consumption flow
 * 
 * This component shows:
 * 1. How to obtain an access token
 * 2. How to include the token in API requests
 * 3. How to handle API responses
 * 4. Error handling and token refresh
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@Component
public class DatavalidIntegrationExample {

    private final DatavalidTokenService tokenService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // API Endpoints
    private static final String DEMO_BASE_URL = "https://gateway.apiserpro.serpro.gov.br/datavalid-demonstracao";
    private static final String PROD_BASE_URL = "https://gateway.apiserpro.serpro.gov.br/datavalid";
    private static final String API_VERSION = "/v4";
    private static final String PF_BASICA_ENDPOINT = "/pf-basica";
    private static final String APP_PROVA_VIDA_ENDPOINT = "/app-provadevida";

    public DatavalidIntegrationExample(
            DatavalidTokenService tokenService,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Example 1: Basic CPF validation
     * Calls the /v4/pf-basica endpoint to validate citizen data
     * 
     * @param cpf CPF number
     * @param nomeValidacao Name for validation
     * @param dataNascimento Birth date for validation
     * @return API response as JSON
     */
    public String validarCpfBasico(String cpf, String nomeValidacao, String dataNascimento) {
        try {
            // Step 1: Obtain valid token
            String token = tokenService.getToken();
            System.out.println("✓ Token obtido com sucesso");

            // Step 2: Prepare request body
            String requestBody = """
                {
                    "cpf": "%s",
                    "validacao": {
                        "nome": "%s",
                        "situacao_cpf": "regular",
                        "data_nascimento": "%s",
                        "sexo": "M",
                        "nacionalidade": 1,
                        "nome_mae": "Maria Silva"
                    }
                }
                """.formatted(cpf, nomeValidacao, dataNascimento);

            System.out.println("✓ Corpo da requisição preparado");

            // Step 3: Make request to DataValid API
            String response = restClient.post()
                    .uri(DEMO_BASE_URL + API_VERSION + PF_BASICA_ENDPOINT)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            System.out.println("✓ Resposta recebida da API DataValid");
            
            // Step 4: Parse and log response
            JsonNode responseJson = objectMapper.readTree(response);
            System.out.println("Resultado da validação: " + responseJson.toPrettyString());

            return response;

        } catch (IllegalStateException e) {
            System.err.println("✗ Erro de configuração: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("✗ Erro na requisição: " + e.getMessage());
            throw new RuntimeException("Falha ao validar CPF", e);
        }
    }

    /**
     * Example 2: Create PIN for biometric validation
     * Calls the /v4/app-provadevida/criar-pin endpoint
     * 
     * @param cpf CPF number for PIN creation
     * @return PIN response with PIN code
     */
    public String criarPinProvaDeVida(String cpf) {
        try {
            // Step 1: Obtain token
            String token = tokenService.getToken();
            System.out.println("✓ Token obtido para criar PIN");

            // Step 2: Prepare request
            String requestBody = """
                {
                    "cpf": "%s",
                    "expira_em": 30,
                    "qtd_tentativas": 5
                }
                """.formatted(cpf);

            // Step 3: Make request
            String response = restClient.post()
                    .uri(DEMO_BASE_URL + API_VERSION + APP_PROVA_VIDA_ENDPOINT + "/criar-pin")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            System.out.println("✓ PIN criado com sucesso");
            
            JsonNode responseJson = objectMapper.readTree(response);
            String pin = responseJson.get("pin").asText();
            System.out.println("PIN gerado: " + pin);

            return response;

        } catch (Exception e) {
            System.err.println("✗ Erro ao criar PIN: " + e.getMessage());
            throw new RuntimeException("Falha ao criar PIN", e);
        }
    }

    /**
     * Example 3: Handle token expiration and retry
     * Demonstrates how to handle 401 responses and refresh token
     * 
     * @param cpf CPF to validate
     * @return API response
     */
    public String validarComRetentativa(String cpf) {
        try {
            return tentarValidacao(cpf);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401")) {
                System.out.println("⚠ Token expirado, renovando...");
                tokenService.clearTokenCache(); // Clear cache to force refresh
                
                try {
                    return tentarValidacao(cpf);
                } catch (RuntimeException retryError) {
                    System.err.println("✗ Falha na retentativa: " + retryError.getMessage());
                    throw retryError;
                }
            }
            throw e;
        }
    }

    /**
     * Helper method for validation attempt
     */
    private String tentarValidacao(String cpf) {
        String token = tokenService.getToken();

        String requestBody = """
            {
                "cpf": "%s",
                "validacao": {
                    "nome": "Teste Usuario",
                    "situacao_cpf": "regular",
                    "data_nascimento": "1990-01-01"
                }
            }
            """.formatted(cpf);

        return restClient.post()
                .uri(DEMO_BASE_URL + API_VERSION + PF_BASICA_ENDPOINT)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example 4: Check service configuration
     * Useful for health checks and startup validation
     * 
     * @return Configuration status
     */
    public ConfigurationStatus verificarConfiguracao() {
        boolean configured = tokenService.isConfigured();
        System.out.println(configured 
            ? "✓ DataValid configurado e pronto para uso"
            : "✗ DataValid não está configurado"
        );
        
        return new ConfigurationStatus(configured, "DataValid API");
    }

    /**
     * Example 5: Production environment usage
     * Shows how to switch from demo to production environment
     * 
     * @param cpf CPF to validate
     * @param useProduction Whether to use production (true) or demo (false)
     * @return API response
     */
    public String validarComAmbienteConfiguravel(String cpf, boolean useProduction) {
        String baseUrl = useProduction ? PROD_BASE_URL : DEMO_BASE_URL;
        String ambiente = useProduction ? "PRODUÇÃO" : "DEMONSTRAÇÃO";
        
        System.out.println("Usando ambiente: " + ambiente);

        String token = tokenService.getToken();

        String requestBody = """
            {
                "cpf": "%s",
                "validacao": {
                    "nome": "Teste Usuario",
                    "situacao_cpf": "regular",
                    "data_nascimento": "1990-01-01"
                }
            }
            """.formatted(cpf);

        return restClient.post()
                .uri(baseUrl + API_VERSION + PF_BASICA_ENDPOINT)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    // Helper classes

    /**
     * Configuration status response
     */
    public static class ConfigurationStatus {
        private final boolean configured;
        private final String service;
        private final long timestamp;

        public ConfigurationStatus(boolean configured, String service) {
            this.configured = configured;
            this.service = service;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isConfigured() {
            return configured;
        }

        public String getService() {
            return service;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "ConfigurationStatus{" +
                    "service='" + service + '\'' +
                    ", configured=" + configured +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * Usage example (can be run as CommandLineRunner for testing)
     */
    public static void printUsageExample() {
        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════════╗
            ║          DataValid API - Exemplo de Integração                 ║
            ╚════════════════════════════════════════════════════════════════╝
            
            EXEMPLO 1: Validação Básica de CPF
            ────────────────────────────────────
            String resultado = example.validarCpfBasico(
                "12345678901",
                "João Silva",
                "1990-01-01"
            );
            
            EXEMPLO 2: Criar PIN para Prova de Vida
            ─────────────────────────────────────
            String pin = example.criarPinProvaDeVida("12345678901");
            
            EXEMPLO 3: Validar com Retentativa (em caso de token expirado)
            ──────────────────────────────────────────────────────────
            String resultado = example.validarComRetentativa("12345678901");
            
            EXEMPLO 4: Verificar Configuração
            ────────────────────────────────
            ConfigurationStatus status = example.verificarConfiguracao();
            
            EXEMPLO 5: Usar Ambiente de Produção
            ──────────────────────────────────
            String resultado = example.validarComAmbienteConfiguravel(
                "12345678901",
                true  // true = Produção, false = Demonstração
            );
            
            FLUXO COMPLETO:
            ───────────────
            1. Obter token automaticamente (com cache)
            2. Preparar corpo da requisição
            3. Incluir token no header Authorization
            4. Fazer requisição para endpoint desejado
            5. Processar resposta
            6. Renovar token automaticamente se expirar
            
            ╚════════════════════════════════════════════════════════════════╝
            """);
    }
}
