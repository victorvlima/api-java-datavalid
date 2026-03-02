# Sistema de Exceções da API SERPRO DataValid

Este documento descreve a estrutura completa de exceções implementada para tratamento de erros da API SERPRO DataValid.

## 📋 Estrutura de Classes

### Hierarquia de Exceções

```
RuntimeException
    └── DataValidException (base)
        ├── BadRequestException (400)
        ├── UnauthorizedException (401)
        ├── ForbiddenException (403)
        ├── NotFoundException (404)
        ├── RequestTooLargeException (413)
        ├── UnprocessableEntityException (422)
        ├── InternalServerErrorException (500)
        ├── BadGatewayException (502)
        ├── ServiceUnavailableException (503)
        └── GatewayTimeoutException (504)
```

## 🔧 Componentes Principais

### 1. **DataValidException**
Exceção base que estende `RuntimeException`. Contém informações sobre:
- Status HTTP
- Código de erro (ex: DV042)
- Mensagem descritiva
- Link para documentação

### 2. **DataValidErrorCode** (Enum)
Enumera todos os códigos de erro da API com suas descrições:

#### Categorias de Códigos:

**LGPD e Requisitos Mínimos**
- `DV001` - Dados de menor de idade
- `DV002` - Dados não atendem requisitos mínimos

**Validação de Documentos**
- `DV010` - CPF inválido
- `DV020` - CNPJ inválido
- `DV011-DV022` - Domínios inválidos (nacionalidade, sexo, UF, etc.)

**Impressão Digital**
- `DV030-DV036` - Erros relacionados a biometria digital

**Validação Facial**
- `DV040-DV047` - Erros de imagem facial
- `DV048-DV053` - Erros de face de referência
- `DV061-DV062` - Erros de liveness (prova de vida)
- `DV170-DV173` - Erros de PIN e prova de vida

**OCR da CNH**
- `DV079-DV089` - Erros de leitura de documento

**QR Code**
- `DV101-DV112` - Erros de leitura de QR Code

**Serviços**
- `DV150-DV152` - Serviços de integração indisponíveis

### 3. **DataValidExceptionFactory**
Factory para criar exceções apropriadas baseadas na resposta da API. Métodos principais:

```java
// Cria exceção baseada em status HTTP e corpo da resposta
DataValidException createException(int statusCode, String responseBody)

// Verifica se status indica sucesso (2xx)
boolean isSuccessStatus(int statusCode)

// Verifica se é erro do cliente (4xx)
boolean isClientError(int statusCode)

// Verifica se é erro do servidor (5xx)  
boolean isServerError(int statusCode)
```

### 4. **DataValidErrorResponse**
Modelo para capturar respostas de erro da API:
```json
{
  "code": "DV042",
  "link": "https://apicenter.estaleiro.serpro.gov.br/...",
  "message": "Mensagem opcional"
}
```

## 💡 Como Usar

### Exemplo 1: Capturando Erros Específicos

```java
try {
    FacialPfResponse response = facialPfService.validateFacial(cpf, photoPath, token);
    // Processar resposta de sucesso
    
} catch (UnprocessableEntityException e) {
    // Erro 422 - requisição não processada
    String errorCode = e.getErrorCode(); // Ex: "DV042"
    DataValidErrorCode code = e.getErrorCodeEnum();
    String description = code.getDescription();
    
    System.err.println("Erro de validação: " + errorCode);
    System.err.println("Descrição: " + description);
    System.err.println("Link: " + e.getLink());
    
} catch (UnauthorizedException e) {
    // Erro 401 - token inválido
    System.err.println("Token de autenticação inválido");
    
} catch (ServiceUnavailableException e) {
    // Erro 503 - serviço temporariamente indisponível
    System.err.println("Serviço temporariamente indisponível. Tente novamente mais tarde.");
    
} catch (DataValidException e) {
    // Outros erros da API
    System.err.println("Erro na API: " + e.getMessage());
    System.err.println("Status HTTP: " + e.getHttpStatus());
}
```

### Exemplo 2: Tratamento Genérico

```java
try {
    FacialPfResponse response = facialPfService.validateFacial(cpf, photoPath, token);
    // processar resposta
    
} catch (DataValidException e) {
    if (DataValidExceptionFactory.isClientError(e.getHttpStatus())) {
        // Erro do cliente (4xx) - corrigir requisição
        logger.warn("Erro na requisição: " + e.getMessage());
        
    } else if (DataValidExceptionFactory.isServerError(e.getHttpStatus())) {
        // Erro do servidor (5xx) - tentar novamente mais tarde
        logger.error("Erro no servidor: " + e.getMessage());
    }
}
```

### Exemplo 3: Tratando Códigos DV Específicos

```java
try {
    FacialPfResponse response = facialPfService.validateFacial(cpf, photoPath, token);
    
} catch (UnprocessableEntityException e) {
    DataValidErrorCode errorCode = e.getErrorCodeEnum();
    
    switch (errorCode) {
        case DV042:
            System.err.println("Tamanho da imagem inválido. Ajuste o tamanho da foto.");
            break;
            
        case DV041:
            System.err.println("Face não reconhecida. Envie uma foto mais clara.");
            break;
            
        case DV040:
            System.err.println("CPF não possui foto cadastrada nas bases do governo.");
            break;
            
        case DV001:
            System.err.println("Não é possível validar dados de menores de idade.");
            break;
            
        default:
            System.err.println("Erro: " + errorCode.getDescription());
    }
}
```

## 📊 Exceções HTTP por Status Code

| Status | Exceção | Descrição |
|--------|---------|-----------|
| 400 | `BadRequestException` | Requisição inválida - problema nos campos enviados |
| 401 | `UnauthorizedException` | Não autenticado - token inválido |
| 403 | `ForbiddenException` | Não autorizado - sem permissão |
| 404 | `NotFoundException` | Endpoint não encontrado |
| 413 | `RequestTooLargeException` | Requisição maior que 3MB |
| 422 | `UnprocessableEntityException` | Requisição não processada - contém código DV |
| 500 | `InternalServerErrorException` | Erro interno no servidor |
| 502 | `BadGatewayException` | Problema no gateway |
| 503 | `ServiceUnavailableException` | Serviço temporariamente indisponível |
| 504 | `GatewayTimeoutException` | Timeout no gateway |

## 🎯 Boas Práticas

1. **Sempre capture exceções específicas primeiro**, depois as genéricas:
   ```java
   catch (UnprocessableEntityException e) { }
   catch (DataValidException e) { }
   ```

2. **Use o enum DataValidErrorCode** para decisões programáticas em vez de comparar strings.

3. **Log apropriado**:
   - Erros 4xx (cliente): `logger.warn()`
   - Erros 5xx (servidor): `logger.error()`

4. **Não exponha detalhes técnicos** ao usuário final. Use mensagens amigáveis.

5. **Para erros 5xx**, implemente retry com backoff exponencial.

## 🔗 Referências

- [Documentação Oficial - Códigos de Retorno](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/)
- [API Reference](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/api_reference/)

## ✅ Bilhetagem

**Códigos faturados:**
- 200 (OK) - sucesso
- 422 com DV001 - dados de menor (mesmo sendo erro)

**Códigos NÃO faturados:**
- 204, 400, 401, 403, 413
- 422 (exceto DV001)
- 500, 502, 503, 504
