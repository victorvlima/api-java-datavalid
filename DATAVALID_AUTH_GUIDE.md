# DataValid API Autenticação - Guia de Uso

Este documento descreve como usar a classe `DatavalidAuth` para obter tokens de autenticação da API DataValid do SERPRO.

## Visão Geral

A autenticação na API DataValid utiliza o protocolo **OAuth2** com fluxo de credenciais do cliente (client credentials). O token obtido tem validade de **1 hora** e deve ser renovado quando expirar.

## Requisitos

- Consumer Key e Consumer Secret (obtidas em https://cliente.serpro.gov.br/)
- Spring Boot 4.0.3 ou superior
- Java 21 ou superior

## Configuração

### 1. Variáveis de Ambiente

Configure as seguintes variáveis de ambiente:

```bash
export DATAVALID_CONSUMER_KEY="sua_consumer_key_aqui"
export DATAVALID_CONSUMER_SECRET="sua_consumer_secret_aqui"
```

### 2. Arquivo application.yaml

Ou configure diretamente no arquivo `application.yaml`:

```yaml
datavalid:
  consumer:
    key: sua_consumer_key_aqui
    secret: sua_consumer_secret_aqui
  use:
    demo: true  # true para demonstração, false para produção
```

## Uso da API

### Opção 1: Usar DatavalidTokenService (Recomendado)

```java
@Service
public class MeuServico {
    
    private final DatavalidTokenService tokenService;
    
    public MeuServico(DatavalidTokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    public void fazerRequisicaoParaDatavalid() {
        // Obtém um token válido (usa cache se não expirou)
        String token = tokenService.getToken();
        
        // Usar o token em requisições HTTP
        // Authorization: Bearer {token}
    }
    
    public void renovarToken() {
        // Força renovação do token
        String novoToken = tokenService.refreshToken();
    }
}
```

### Opção 2: Usar DatavalidAuth Diretamente

```java
@Component
public class MeuComponente {
    
    private final DatavalidAuth datavalidAuth;
    
    public MeuComponente(DatavalidAuth datavalidAuth) {
        this.datavalidAuth = datavalidAuth;
    }
    
    public void obterToken() {
        try {
            // Obter novo token
            DatavalidAuth.TokenResponse response = datavalidAuth.obtainToken(
                consumerKey,
                consumerSecret,
                true  // true para demo, false para produção
            );
            
            String accessToken = response.getAccessToken();
            Long expiresIn = response.getExpiresIn();
            String tokenType = response.getTokenType();
            
            System.out.println("Token: " + accessToken);
            System.out.println("Válido por: " + expiresIn + " segundos");
            System.out.println("Tipo: " + tokenType);
            
        } catch (DatavalidAuth.TokenAcquisitionException e) {
            System.err.println("Erro ao obter token: " + e.getMessage());
        }
    }
}
```

## Endpoints REST

### 1. Obter Novo Token

```bash
POST /api/datavalid/auth/token

Response:
{
  "success": true,
  "message": "Token obtained successfully",
  "access_token": "eyJ4NXQi...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "default"
}
```

### 2. Validar Configuração

```bash
GET /api/datavalid/auth/validate

Response:
{
  "isConfigured": true,
  "message": "DataValid service is properly configured",
  "status": "READY"
}
```

### 3. Renovar Token

```bash
POST /api/datavalid/auth/refresh

Response:
{
  "success": true,
  "message": "Token refreshed successfully",
  "access_token": "eyJ4NXQi...",
  "timestamp": 1708372800000
}
```

## Estrutura de Resposta do Token

```json
{
  "access_token": "eyJ4NXQiOiJSUzI1NiIsImtpZCI6IjEifQ...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "default"
}
```

### Campos

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `access_token` | String | Token JWT a ser usado nas requisições (Bearer Token) |
| `token_type` | String | Tipo do token (sempre "Bearer" para OAuth2) |
| `expires_in` | Long | Tempo de expiração em segundos (geralmente 3600 = 1 hora) |
| `scope` | String | Escopo de permissões (padrão: "default") |

## Fluxo de Autenticação

1. **Codificar Credenciais**
   - Consumer Key + ":" + Consumer Secret → Base64

2. **Fazer Requisição POST**
   ```
   POST https://gateway.apiserpro.serpro.gov.br/token
   Authorization: Basic {encoded_credentials}
   Content-Type: application/x-www-form-urlencoded
   
   grant_type=client_credentials
   ```

3. **Receber Token**
   - Response contém `access_token` no formato JWT

4. **Usar Token em Requisições**
   ```
   Authorization: Bearer {access_token}
   ```

## Cache de Token

A classe `DatavalidAuth` implementa cache automático de token:

- **Token é armazenado em cache** após a primeira obtenção
- **Validade é verificada** antes de usar o cache
- **5 minutos de margem de segurança** são considerados (token renovado 5 min antes da expiração real)
- **Cache pode ser limpo** manualmente com `clearCache()`

## Tratamento de Erros

### TokenAcquisitionException

Lançada quando falha ao obter o token:

```java
try {
    DatavalidAuth.TokenResponse response = datavalidAuth.obtainToken(key, secret, true);
} catch (DatavalidAuth.TokenAcquisitionException e) {
    System.err.println("Erro: " + e.getMessage());
    // Implementar retry ou tratamento apropriado
}
```

### IllegalStateException

Lançada quando credenciais não estão configuradas:

```java
try {
    String token = tokenService.getToken();
} catch (IllegalStateException e) {
    System.err.println("Erro de configuração: " + e.getMessage());
}
```

## Exemplo Completo de Uso

```java
@RestController
@RequestMapping("/api/datavalid")
public class DatavalidController {
    
    private final DatavalidTokenService tokenService;
    private final RestClient restClient;
    
    public DatavalidController(DatavalidTokenService tokenService, RestClient.Builder restClientBuilder) {
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.build();
    }
    
    @PostMapping("/validar-cpf")
    public ResponseEntity<?> validarCpf(@RequestParam String cpf) {
        try {
            // 1. Obter token válido
            String token = tokenService.getToken();
            
            // 2. Preparar requisição
            String body = """
                {
                    "cpf": "%s",
                    "validacao": {
                        "nome": "João Silva",
                        "situacao_cpf": "regular",
                        "data_nascimento": "1990-01-01"
                    }
                }
                """.formatted(cpf);
            
            // 3. Fazer requisição para DataValid
            String response = restClient.post()
                .uri("https://gateway.apiserpro.serpro.gov.br/datavalid/v4/pf-basica")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
            
            return ResponseEntity.ok(response);
            
        } catch (DatavalidAuth.TokenAcquisitionException e) {
            return ResponseEntity.status(500)
                .body("Erro ao obter token: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of(
            "configurado", tokenService.isConfigured(),
            "ambiente", "demonstração"
        ));
    }
}
```

## Links Úteis

- [Documentação DataValid](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/)
- [Quick Start Guide](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/quick_start/)
- [API Reference](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/api_reference/)
- [SERPRO Client Area](https://cliente.serpro.gov.br/)

## Dicas e Boas Práticas

1. **Armazenar credenciais com segurança**: Use variáveis de ambiente, não hardcode
2. **Renovar token antes da expiração**: O sistema faz isso automaticamente com 5 min de margem
3. **Usar `DatavalidTokenService`**: Mais simples e com melhor abstração
4. **Implementar retry logic**: Caso a requisição de token falhe
5. **Monitorar tokens expirados**: Verifique HTTP 401 na resposta da API

## Troubleshooting

### "Credenciais não configuradas"
- Verificar variáveis de ambiente
- Verificar application.yaml
- Reiniciar aplicação após mudanças de configuração

### "Erro ao obter token"
- Verificar Consumer Key e Secret
- Verificar conectividade com gateway.apiserpro.serpro.gov.br
- Verificar logs da aplicação para detalhes

### "Token expirado"
- Sistema renova automaticamente 5 minutos antes da expiração
- Se receber 401, chame `refreshToken()`

---

Documento gerado com base em: https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/quick_start/
