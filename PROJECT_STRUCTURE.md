# 📁 Estrutura do Projeto DataValid Auth

```
api-java-datavalid/
│
├── 📚 DOCUMENTAÇÃO
│   ├── DATAVALID_AUTH_GUIDE.md              # Guia completo de uso
│   ├── IMPLEMENTATION_SUMMARY.md            # Resumo da implementação
│   └── README.md                            # README original
│
├── 📦 CÓDIGO FONTE
│   └── src/main/java/br/gov/ce/fortaleza/fd/datavalid/
│       │
│       ├── 🔐 model/
│       │   └── DatavalidAuth.java           # CLASSE PRINCIPAL
│       │       ├─ OAuth2 Client Credentials Flow
│       │       ├─ TokenResponse (classe interna)
│       │       ├─ TokenAcquisitionException (classe interna)
│       │       ├─ obtainToken()
│       │       ├─ getValidToken()
│       │       ├─ getCachedToken()
│       │       └─ clearCache()
│       │
│       ├── 🔧 service/
│       │   └── DatavalidTokenService.java   # SERVIÇO DE TOKENS
│       │       ├─ Integração com Spring
│       │       ├─ getToken()
│       │       ├─ refreshToken()
│       │       ├─ obtainTokenResponse()
│       │       └─ isConfigured()
│       │
│       ├── 🌐 controller/
│       │   └── DatavalidAuthController.java # ENDPOINTS REST
│       │       ├─ POST /api/datavalid/auth/token
│       │       ├─ GET /api/datavalid/auth/validate
│       │       └─ POST /api/datavalid/auth/refresh
│       │
│       ├── 📘 example/
│       │   └── DatavalidIntegrationExample.java # EXEMPLOS DE USO
│       │       ├─ validarCpfBasico()
│       │       ├─ criarPinProvaDeVida()
│       │       ├─ validarComRetentativa()
│       │       ├─ verificarConfiguracao()
│       │       └─ validarComAmbienteConfiguravel()
│       │
│       └── Datavalid.java                   # Classe existente (client API)
│
├── 🧪 TESTES
│   └── src/test/java/br/gov/ce/fortaleza/fd/datavalid/
│       └── model/
│           └── DatavalidAuthTest.java       # TESTES UNITÁRIOS
│               ├─ testObtainTokenSuccess()
│               ├─ testObtainTokenDemoEnvironment()
│               ├─ testCredentialsEncoding()
│               ├─ testTokenCaching()
│               ├─ testTokenAcquisitionFailure()
│               ├─ testGetValidTokenWithCache()
│               ├─ testClearCache()
│               └─ testHttpHeaders()
│
├── ⚙️ CONFIGURAÇÃO
│   ├── src/main/resources/
│   │   └── application.yaml                 # CONFIGURAÇÕES
│   │       ├─ datavalid.consumer.key
│   │       ├─ datavalid.consumer.secret
│   │       └─ datavalid.use.demo
│   │
│   ├── pom.xml                              # Maven config (não modificado)
│   ├── mvnw                                 # Maven Wrapper
│   └── mvnw.cmd                             # Maven Wrapper (Windows)
│
└── 📋 BUILD
    └── target/                              # Saída de compilação
```

---

## 🔑 Classes e Métodos Principais

### DatavalidAuth.java
```java
// Obter novo token
TokenResponse obtainToken(String key, String secret, boolean isDemo)

// Obter token válido (com cache)
String getValidToken(String key, String secret, boolean isDemo)

// Acessar token em cache
String getCachedToken()

// Limpar cache
void clearCache()

// Resposta do token
class TokenResponse {
    String accessToken
    String tokenType      // "Bearer"
    Long expiresIn        // segundos (3600)
    String scope          // "default"
}

// Exceção customizada
class TokenAcquisitionException extends RuntimeException
```

### DatavalidTokenService.java
```java
// Obter token (automaticamente com cache)
String getToken()

// Renovar token
String refreshToken()

// Detalhes do token
TokenResponse obtainTokenResponse()

// Verificar configuração
boolean isConfigured()

// Limpar cache
void clearTokenCache()
```

### DatavalidAuthController.java
```java
// Endpoints REST
POST   /api/datavalid/auth/token      → Obter novo token
GET    /api/datavalid/auth/validate   → Validar configuração
POST   /api/datavalid/auth/refresh    → Renovar token
```

---

## 🚀 Fluxo de Uso Recomendado

```
1. CONFIGURAR CREDENCIAIS
   ↓
   Variáveis de ambiente:
   - DATAVALID_CONSUMER_KEY
   - DATAVALID_CONSUMER_SECRET
   ↓

2. INICIAR APLICAÇÃO
   ↓
   Spring injeta DatavalidTokenService
   ↓

3. USAR EM SEUS SERVIÇOS
   ↓
   @Autowired DatavalidTokenService tokenService
   String token = tokenService.getToken()
   ↓

4. INCLUIR EM REQUISIÇÕES
   ↓
   Authorization: Bearer {token}
   ↓

5. GERENCIAMENTO AUTOMÁTICO
   ↓
   Cache validado a cada uso
   Renovação automática + 5 min antes de expirar
```

---

## 📊 Fluxo de Dados

```
REQUISIÇÃO DO TOKEN
─────────────────

┌──────────────────────┐
│ DatavalidTokenService│
│   (Serviço Spring)   │
└──────────────────────┘
           │
           ├─ Verifica credenciais configuradas
           │
           ├─ Chama DatavalidAuth.obtainToken()
           │
           └─ Retorna token (com cache)
                    │
                    ├─ Codifica: Key + ":" + Secret → Base64
                    │
                    ├─ POST /token
                    │   ├─ Header: Authorization: Basic {encoded}
                    │   ├─ Header: Content-Type: application/x-www-form-urlencoded
                    │   └─ Body: grant_type=client_credentials
                    │
                    └─ Resposta:
                        {
                          "access_token": "eyJ...",
                          "token_type": "Bearer",
                          "expires_in": 3600,
                          "scope": "default"
                        }


FLUXO COM CACHE
───────────────

┌─────────────────────────────────────────────┐
│ Primeira Requisição: getToken()             │
├─────────────────────────────────────────────┤
│ 1. Obter token do gateway SERPRO (3600 seg) │
│ 2. Armazenar em cache                        │
│ 3. Calcular expiração - 300 seg (5 min)     │
│ 4. Retornar token                            │
└─────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────┐
│ Próximas Requisições: getToken()            │
├─────────────────────────────────────────────┤
│ 1. Verificar se cache é válido               │
│ 2. Se válido: Retornar do cache (rápido)   │
│ 3. Se expirado: Obter novo token             │
└─────────────────────────────────────────────┘
```

---

## 🔒 Segurança

✅ **Credenciais Protegidas**
- Nunca hardcode credenciais no código
- Use variáveis de ambiente
- Nunca commit de .env ou credentials

✅ **Codificação Base64**
- Credenciais codificadas automaticamente
- Transmissão HTTPS (obrigatório)

✅ **Tokens JWT**
- Tempo de vida curto (3600 seg)
- Renovação automática
- Margem de segurança de 5 minutos

✅ **Exceções Tratadas**
- TokenAcquisitionException captura erros
- Sem exposição de detalhes internos

---

## ✨ Funcionalidades Implementadas

| Funcionalidade | Status | Details |
|---|---|---|
| OAuth2 Client Credentials | ✅ | Fluxo completo implementado |
| Codificação Base64 | ✅ | Automática para credenciais |
| Cache de Token | ✅ | Com validação de expiração |
| Margem de Segurança | ✅ | 5 minutos antes de expirar |
| Ambientes Demo/Prod | ✅ | Toggle configurável |
| Injeção Spring | ✅ | @Component e @Service |
| REST Endpoints | ✅ | 3 endpoints implementados |
| Testes Unitários | ✅ | 8 testes cobrindo casos principais |
| Documentação | ✅ | Guia completo + exemplos |
| Tratamento de Erros | ✅ | Exceções customizadas |

---

## 📞 Suporte e Links

| Recurso | Link |
|---------|------|
| Documentação DataValid | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/ |
| Quick Start | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/quick_start/ |
| API Reference | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/api_reference/ |
| SERPRO Client Area | https://cliente.serpro.gov.br/ |
| Central de Ajuda | https://centraldeajuda.serpro.gov.br/ |

---

**Implementação Completa e Pronta para Uso! 🎉**
