# 📸 Validação Facial com CPF - Guia Completo

## Visão Geral

A validação facial permite verificar se uma foto de um usuário corresponde aos dados biométricos registrados na base de dados do governo brasileiro. Combina validação de CPF com reconhecimento facial biométrico.

## Fluxo de Funcionamento

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Cliente fornece CPF + Foto (Base64 ou Arquivo)           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ 2. Serviço valida inputs                                    │
│    ├─ CPF: 11 dígitos                                       │
│    ├─ Foto: JPG/PNG/PDF, 250x250px min, 3MB max           │
│    └─ Qualidade: Padrão ICAO                                │
│                      ↓                                       │
│ 3. Obter token OAuth2 automaticamente                       │
│                      ↓                                       │
│ 4. POST /v4/pf-facial (com autenticação Bearer)            │
│    ├─ Body: CPF + FotoBase64 + dados validação (opcional)  │
│    └─ Headers: Authorization, Content-Type                  │
│                      ↓                                       │
│ 5. SERPRO DataValid processa                                │
│    ├─ Verifica foto na base biométrica                      │
│    ├─ Compara com foto no CPF                               │
│    └─ Retorna score de similaridade (0.0 a 1.0)           │
│                      ↓                                       │
│ 6. Resposta com resultado                                   │
│    ├─ foto_existe: true/false                               │
│    ├─ face_similaridade: 0.0 a 1.0                         │
│    └─ rfb (opcional): dados CPF validados                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Requisitos de Imagem

### Formatos Aceitos
- **JPG/JPEG**: Formato recomendado
- **PNG**: Lossless, ideal para fotos high-quality
- **PDF**: Para documentos com foto

### Dimensões
- **Mínimo**: 250×250 pixels (área do rosto)
- **Recomendado**: 750×750 pixels
- **Máximo arquivo**: 3MB (total da requisição)

### Qualidade
A imagem deve seguir as recomendações ICAO (International Civil Aviation Organization):
- ✅ Fundo uniforme e liso
- ✅ Rosto totalmente visível
- ✅ Olhos abertos e visíveis
- ✅ Sem óculos escuro ou máscara
- ✅ Iluminação adequada (sem shadows, brilho)
- ✅ Sem efeito de flash ou olhos vermelhos
- ✅ Proporção altura/largura correta
- ✅ Uma única face na imagem

- ❌ Imagens desfocadas ou borradas
- ❌ Muito escura ou muito clara
- ❌ Com chapéu, gorro ou acessórios que cobrem rosto
- ❌ Oclusão (máscara, óculos, etc.)
- ❌ Duas faces na mesma imagem
- ❌ Selfie + documento juntamente

## Implementação

### 1. Modelos de Dados

**FacialPf** - Requisição
```java
public class FacialPf {
    private String cpf;                          // CPF (11 dígitos)
    private FacialData foto;                     // Foto base64
    private ValidationData validacao;            // Dados para validação cruzada
    
    public static class FacialData {
        private String imagem;                   // Imagem em Base64
    }
    
    public static class ValidationData {
        private String nome;                     // Nome
        private String dataNascimento;           // YYYY-MM-DD
        private String situacaoCpf;              // "regular", "cancelada", etc
        private String sexo;                     // "M" ou "F"
        private Integer nacionalidade;           // 1 = Brasileiro
    }
}
```

**FacialPfResponse** - Resposta
```java
public class FacialPfResponse {
    private Boolean fotoExiste;                  // Foto existe na base
    private FaceValidationResult foto;           // Resultado facial
    private CpfValidationResult rfb;             // Resultado CPF (opcional)
    
    public static class FaceValidationResult {
        private Double faceSimilaridade;         // 0.0 a 1.0
    }
    
    public static class CpfValidationResult {
        private Boolean nome;                    // Nome válido
        private Boolean situacaoCpf;             // Situação válida
        private Boolean dataNascimento;          // Data válida
    }
}
```

### 2. Serviço

**FacialValidationService** - Operações
```java
// Validar com base64
FacialPfResponse validateFacialWithCpf(
    String cpf, 
    String photoBase64
);

// Validar com arquivo
FacialPfResponse validateFacialWithFile(
    String cpf, 
    File photoFile
) throws IOException;

// Validar threshold
boolean validateSimilarityThreshold(FacialPfResponse response, Double threshold);

// Obter percentual
Double getSimilarityPercentage(FacialPfResponse response);
```

## Endpoints REST

### 1. Validar com Base64

```bash
POST /api/datavalid/facial/validate-with-base64
Content-Type: application/json
Authorization: Bearer {token}

{
  "cpf": "25774435016",
  "foto": {
    "imagem": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
  },
  "validacao": {
    "nome": "João Silva",
    "data_nascimento": "1990-01-15",
    "sexo": "M",
    "nacionalidade": 1
  }
}
```

**Resposta (200 OK)**
```json
{
  "success": true,
  "message": "Facial validation completed",
  "cpf": "25774435016",
  "fotoExiste": true,
  "similarity": 92.5,
  "meetsThreshold": true,
  "details": {
    "faceSimilaridade": 0.925
  },
  "timestamp": 1708372800000
}
```

### 2. Validar com Arquivo

```bash
POST /api/datavalid/facial/validate-with-file?cpf=25774435016&filePath=/path/to/photo.jpg
Content-Type: application/json

{
  "nome": "João Silva",
  "data_nascimento": "1990-01-15"
}
```

### 3. Health Check

```bash
GET /api/datavalid/facial/health

{
  "service": "FacialValidation",
  "status": "UP",
  "supportedFormats": ["JPG", "PNG", "PDF"],
  "minResolution": "250x250 pixels",
  "maxFileSize": "3MB"
}
```

## Uso em Java

### Exemplo 1: Base64

```java
@Service
public class UsuarioService {
    @Autowired
    private FacialValidationService facialService;
    
    public void registrarUsuario(String cpf, String photoBase64) {
        FacialPfResponse response = facialService.validateFacialWithCpf(
            cpf,
            photoBase64
        );
        
        if (facialService.validateSimilarityThreshold(response)) {
            // Usuário validado
            System.out.println("Validação: OK");
        } else {
            // Rejeitar
            System.out.println("Validação FALHOU");
        }
    }
}
```

### Exemplo 2: Arquivo

```java
public void validarComArquivo(String cpf, String caminhoFoto) throws IOException {
    File fotoFile = new File(caminhoFoto);
    
    FacialPfResponse response = facialService.validateFacialWithFile(
        cpf,
        fotoFile
    );
    
    Double similarity = facialService.getSimilarityPercentage(response);
    System.out.println("Similaridade: " + similarity + "%");
}
```

### Exemplo 3: Threshold Customizado

```java
public boolean validarComThresholdCustomizado(String cpf, String photo) {
    FacialPfResponse response = facialService.validateFacialWithCpf(cpf, photo);
    
    // Requer 90% de similaridade
    return facialService.validateSimilarityThreshold(response, 0.90);
}
```

## Interpretação de Resultados

### Similaridade Facial

| Score | Interpretação | Ação |
|-------|---|---|
| >= 0.90 | Excelente correspondência | ✅ Aceitar |
| 0.85-0.89 | Boa correspondência | ✅ Aceitar |
| 0.75-0.84 | Correspondência aceitável | ⚠️ Análise manual |
| < 0.75 | Fraca correspondência | ❌ Rejeitar |

### Validação CPF (RFB)

Se `validacao` foi enviada, a resposta contém validações adicionais:
- `nome`: Nome bate com CPF?
- `situacao_cpf`: CPF em situação regular?
- `data_nascimento`: Data de nascimento bate?

## Tratamento de Erros

### Erros Comuns

```java
try {
    FacialPfResponse response = facialService.validateFacialWithCpf(cpf, photo);
    
} catch (IllegalArgumentException e) {
    // Erro de validação de entrada
    System.err.println("CPF ou foto inválidos: " + e.getMessage());
    // Mensagem: "CPF must have 11 digits"
    // Mensagem: "Photo file exceeds maximum size"
    
} catch (RestClientException e) {
    // Erro na requisição à API
    System.err.println("Erro na API: " + e.getMessage());
    // Mensagem: "Facial validation request failed"
    
} catch (IOException e) {
    // Erro ao ler arquivo
    System.err.println("Erro ao ler arquivo: " + e.getMessage());
}
```

### Códigos HTTP

| Código | Significado | Ação |
|--------|---|---|
| 200 | Validação concluída | Analisar resultado |
| 400 | Entrada inválida | Verificar CPF/foto |
| 401 | Token expirado | Renovar token |
| 413 | Foto muito grande | Reduzir tamanho |
| 422 | Imagem não processável | Melhorar qualidade |
| 500 | Erro no servidor | Tentar novamente |

## Boas Práticas

### 1. Validar Inputs
```java
// Validar CPF antes
String cpf = input.replaceAll("[^0-9]", "");
if (cpf.length() != 11) {
    throw new IllegalArgumentException("CPF inválido");
}
```

### 2. Comprimir Imagens
Para reduzir tamanho sem afetar qualidade:
```bash
# ImageMagick
convert photo.jpg -quality 85 -resize 750x750 photo_optimized.jpg
```

### 3. Implementar Retry Logic
```java
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        return facialService.validateFacialWithCpf(cpf, photo);
    } catch (RestClientException e) {
        if (i == maxRetries - 1) throw e;
        Thread.sleep(1000 * (i + 1));
    }
}
```

### 4. Cache de Resultados
```java
// Não revalidar a mesma foto do mesmo CPF frequentemente
@Cacheable(value = "facialValidation", key = "#cpf + #photoHash")
public FacialPfResponse validateAndCache(String cpf, String photo) {
    return facialService.validateFacialWithCpf(cpf, photo);
}
```

### 5. Logging
```java
logger.info("Validando facial - CPF: {}, Similaridade: {}", 
    cpf, similarity);
logger.warn("Validação abaixo do threshold - CPF: {}, Similaridade: {}", 
    cpf, similarity);
```

## Links Úteis

- [Documentação Face Requirements](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/requisitos_face/)
- [API Reference](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/api_reference/)
- [ICAO Standards](https://www.icao.int/Security/FAL/TRIP/Pages/default.aspx)
- [Códigos de Retorno](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/)

## Troubleshooting

### "foto_existe: false"
- Foto não está na base de dados biométrica
- Qualidade da imagem insuficiente
- Usar App DataValid para registrar foto primeiro

### score muito baixo (< 0.75)
- Qualidade ruim da imagem
- Foto antiga ou muito diferente
- Iluminação inadequada
- Usuário diferente da foto

### Erro 422
- Imagem não processável pela IA
- Múltiplas faces ou objetos estranhos
- Ver detalhes: [422 codes](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/#422)

---

**Versão**: 1.0  
**Atualizado**: Fevereiro 2026  
**Status**: ✅ Pronto para Produção
