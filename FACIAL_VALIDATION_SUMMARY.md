# 📸 Validação Facial - Resumo de Implementação

## ✅ Arquivos Criados

### 1. Modelos de Dados

#### [FacialPf.java](src/main/java/br/gov/ce/fortaleza/fd/datavalid/model/FacialPf.java)
- Modelo de requisição para validação facial
- Contém CPF, FacialData (imagem) e ValidationData (opcional)
- Classes internas:
  - `FacialData`: Contém imagem em Base64
  - `ValidationData`: Dados opcionais (nome, data nascimento, sexo, etc.)

#### [FacialPfResponse.java](src/main/java/br/gov/ce/fortaleza/fd/datavalid/model/FacialPfResponse.java)
- Modelo de resposta da validação
- Contém:
  - `fotoExiste`: Se foto existe na base biométrica
  - `foto`: `FaceValidationResult` com score de similaridade (0.0-1.0)
  - `rfb`: Opcional - validação de dados CPF (RFB)
- Métodos úteis:
  - `meetsThreshold(Double threshold)`: Verifica se atende threshold
  - `getSimilarityPercentage()`: Retorna percentual (0-100)

### 2. Serviço

#### [FacialValidationService.java](src/main/java/br/gov/ce/fortaleza/fd/datavalid/service/FacialValidationService.java)
- Serviço Spring para validação facial
- **Métodos principais**:
  - `validateFacialWithCpf(cpf, photoBase64)`: Valida com foto em Base64
  - `validateFacialWithFile(cpf, photoFile)`: Valida com arquivo
  - `validateSimilarityThreshold(response, threshold)`: Verifica threshold
  - `getSimilarityPercentage(response)`: Obter percentual

- **Validações implementadas**:
  - ✅ CPF: 11 dígitos, não vazio
  - ✅ Foto: Base64 válida, tamanho máximo 3MB
  - ✅ Arquivo: Existe, formato (JPG/PNG/PDF), tamanho ≤ 3MB
  - ✅ Resolução mínima: detecção heurística

- **Requisições**: 
  - POST `/v4/pf-facial`
  - Headers: Authorization (Bearer), Content-Type
  - Token obtido automaticamente via `DatavalidTokenService`

### 3. Controlador REST

#### [FacialValidationController.java](src/main/java/br/gov/ce/fortaleza/fd/datavalid/controller/FacialValidationController.java)
- **Endpoints**:
  1. `POST /api/datavalid/facial/validate-with-base64`
     - Request: CPF + foto Base64 + dados validação
     - Response: Resultado com similaridade

  2. `POST /api/datavalid/facial/validate-with-file`
     - Params: cpf, filePath
     - Response: Resultado com similaridade

  3. `GET /api/datavalid/facial/health`
     - Status do serviço

### 4. Exemplos

#### [FacialValidationExample.java](src/main/java/br/gov/ce/fortaleza/fd/datavalid/example/FacialValidationExample.java)
- 4 exemplos práticos:
  1. Validar com base64
  2. Validar com arquivo
  3. Validar com dados de validação cruzada
  4. Validar com threshold customizado

### 5. Testes

#### [FacialValidationServiceTest.java](src/test/java/br/gov/ce/fortaleza/fd/datavalid/service/FacialValidationServiceTest.java)
- 12+ testes unitários:
  - ✅ Validação bem-sucedida (base64 e arquivo)
  - ✅ Validações de entrada (CPF, foto)
  - ✅ Threshold (atende, não atende)
  - ✅ Tratamento de erros
  - ✅ Formatos de arquivo

### 6. Documentação

#### [FACIAL_VALIDATION_GUIDE.md](FACIAL_VALIDATION_GUIDE.md)
- Guia completo de uso
- Requisitos de imagem
- Fluxo de funcionamento
- Exemplos de código
- Interpretação de resultados
- Troubleshooting

---

## 🎯 Funcionalidades Implementadas

| Funcionalidade | Status | Detalhes |
|---|---|---|
| Validação CPF + Foto | ✅ | POST /v4/pf-facial |
| Base64 | ✅ | Suporte completo |
| Arquivo (JPG/PNG/PDF) | ✅ | Leitura e conversão automática |
| Validação de inputs | ✅ | CPF, foto, arquivo, tamanho |
| Score de similaridade | ✅ | 0.0 a 1.0, percentual |
| Threshold validation | ✅ | Default 85%, customizável |
| Dados de validação | ✅ | Nome, data, sexo, nacionalidade |
| RFB validation | ✅ | Validação CPF opcional |
| Token automático | ✅ | DatavalidTokenService integrado |
| Tratamento de erros | ✅ | Exceções customizadas |
| REST endpoints | ✅ | 3 endpoints |
| Testes | ✅ | 12+ testes unitários |
| Documentação | ✅ | Guia completo |

---

## 📊 Arquitetura

```
FacialValidationController
    │
    ├── FacialValidationRequest (DTO)
    │   └── FacialPf, ValidationData
    │
    └── FacialValidationService
        ├── validateFacialWithCpf()
        ├── validateFacialWithFile()
        ├── validateSimilarityThreshold()
        └── DatavalidTokenService (para obter token)
            │
            └── RestClient (Spring WebClient)
                │
                └── POST /v4/pf-facial
                    │
                    └── SERPRO API Gateway
                        │
                        └── DataValid Biometric Engine
```

---

## 🚀 Como Usar

### 1. Injetar o Serviço
```java
@Autowired
private FacialValidationService facialService;
```

### 2. Validar CPF + Foto
```java
// Com Base64
FacialPfResponse response = facialService.validateFacialWithCpf(
    "25774435016",
    "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
);

// Com Arquivo
FacialPfResponse response = facialService.validateFacialWithFile(
    "25774435016",
    new File("/path/to/photo.jpg")
);
```

### 3. Verificar Resultado
```java
// Similaridade atende 85%?
if (facialService.validateSimilarityThreshold(response)) {
    // Usuário validado
}

// Obter percentual
Double similarity = facialService.getSimilarityPercentage(response);
// Ex: 92.5%
```

### 4. Via REST
```bash
curl -X POST http://localhost:8080/api/datavalid/facial/validate-with-base64 \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "25774435016",
    "foto": {"imagem": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."}
  }'
```

---

## 📋 Requisitos de Imagem

| Requisito | Detalhes |
|-----------|----------|
| **Formatos** | JPG, PNG, PDF |
| **Resolução mínima** | 250×250 pixels (face) |
| **Resolução ideal** | 750×750 pixels |
| **Tamanho máximo** | 3MB (total requisição) |
| **Padrão** | ICAO (International Civil Aviation) |
| **Qualidade** | Sem borrão, iluminação adequada |

---

## 🎓 Exemplos de Uso

### Exemplo 1: Validação Completa
```java
String cpf = "25774435016";
String photoBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAUA...";

FacialPfResponse response = facialService.validateFacialWithCpf(cpf, photoBase64);

if (response.getFotoExiste()) {
    Double similarity = facialService.getSimilarityPercentage(response);
    boolean meets = facialService.validateSimilarityThreshold(response);
    
    System.out.println("Similaridade: " + similarity + "%");
    System.out.println("Validação: " + (meets ? "OK" : "FALHOU"));
}
```

### Exemplo 2: Com Validação Cruzada
```java
FacialPf.ValidationData validacao = new FacialPf.ValidationData(
    "João Silva",
    "1990-01-15"
);

FacialPfResponse response = facialService.validateFacialWithCpf(
    cpf,
    photoBase64,
    validacao
);

// Verificar validação CPF também
if (response.getRfb() != null) {
    System.out.println("Nome válido: " + response.getRfb().getNome());
    System.out.println("CPF válido: " + response.getRfb().getSituacaoCpf());
}
```

### Exemplo 3: Threshold Customizado
```java
// Requer 95% de similaridade
boolean approved = facialService.validateSimilarityThreshold(response, 0.95);
```

---

## ⚙️ Configuração

Necessário apenas configurar credenciais DataValid (já feito em `DatavalidAuth`):

```yaml
# application.yaml
datavalid:
  consumer:
    key: ${DATAVALID_CONSUMER_KEY}
    secret: ${DATAVALID_CONSUMER_SECRET}
  use:
    demo: true  # Demo ou Produção
```

---

## 🧪 Testes

Executar testes:
```bash
mvn test -Dtest=FacialValidationServiceTest
```

Testes cobrem:
- Validação bem-sucedida (base64 e arquivo)
- Validação de parâmetros de entrada
- Verificação de thresholds
- Tratamento de erros
- Manipulação de arquivos

---

## 📈 Fluxo de Validação

```
5⃣ Submeter requisição POST /v4/pf-facial
        ↓
1⃣ CPF + FotoBase64 ──→ 2⃣ Validar inputs ──→ 3⃣ Obter token ──→ 4⃣ Preparar request
        ↓
6⃣ SERPRO processa
        ├─ Busca foto na base biométrica
        ├─ Compara com foto do CPF
        └─ Retorna score: 0.0 a 1.0
        ↓
7⃣ Parse resposta ──→ 8⃣ Validar threshold ──→ 9⃣ Retornar resultado
```

---

## ✨ Diferenciais

✅ **Segurança**
- Token obtido automaticamente
- Base64 para transmissão segura
- Validação de inputs robusta

✅ **Facilidade**
- Serviço abstrai complexidade
- Métodos simples e diretos
- Exemplos práticos

✅ **Flexibilidade**
- Base64 ou arquivo
- Threshold customizável
- Validação cruzada opcional

✅ **Robustez**
- Tratamento completo de erros
- Testes unitários
- Logging apropriado

---

## 🔗 Links

| Recurso | URL |
|---------|-----|
| Requisitos Face | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/requisitos_face/ |
| API Reference | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/api_reference/ |
| Códigos Erro | https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/ |
| ICAO Standards | https://www.icao.int/Security/FAL/TRIP/Pages/default.aspx |

---

**Versão**: 1.0  
**Status**: ✅ Pronto para Uso  
**Data**: Fevereiro 2026
