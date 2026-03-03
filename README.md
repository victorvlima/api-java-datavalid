# 🔐 API Java DataValid - API de Validação Facial via SERPRO DataValid

Sistema de reconhecimento e validação facial integrado à API DataValid do SERPRO, desenvolvido para validar a identidade de pessoas físicas através de biometria facial.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura da Aplicação](#arquitetura-da-aplicação)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Funcionalidades](#funcionalidades)
- [Tratamento de Exceções](#tratamento-de-exceções)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Interface Web](#interface-web)
- [Exemplos de Uso](#exemplos-de-uso)
- [Códigos de Retorno](#códigos-de-retorno)

---

## 🎯 Sobre o Projeto

A aplicação **datavalid** é um microsserviço REST desenvolvido em Spring Boot que se integra à API DataValid do SERPRO para realizar validação facial de pessoas físicas. O sistema compara uma foto fornecida com as imagens cadastradas nas bases oficiais (RFB e CNH) e retorna um índice de similaridade com classificação de probabilidade.

### Principais Características:

- ✅ Validação facial através da API SERPRO DataValid v4
- ✅ Upload de fotos via multipart/form-data
- ✅ Conversão automática de similaridade (decimal → percentual)
- ✅ Classificação inteligente por faixas de probabilidade
- ✅ Interface web moderna e responsiva para visualização de resultados
- ✅ Tratamento robusto de exceções com códigos DataValid (DV001-DV173)
- ✅ Logs detalhados para rastreamento e debug
- ✅ Suporte a imagens JPG e PNG

---

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.4.3** - Framework para criação de aplicações Java
- **Spring Web** - Para construção de APIs REST
- **Spring WebFlux** - Para chamadas HTTP assíncronas
- **Jackson** - Serialização/deserialização JSON
- **Maven** - Gerenciamento de dependências
- **Log4j 2.25.3** - Sistema de logs

### Frontend
- **HTML5** - Estrutura da página
- **CSS3** - Estilização moderna e responsiva
- **JavaScript (ES6+)** - Lógica de interação e requisições AJAX
- **Fetch API** - Comunicação com backend

### Integrações Externas
- **API SERPRO DataValid v4** - Validação facial oficial do governo brasileiro

---

## 🏗️ Arquitetura da Aplicação

A aplicação segue uma arquitetura em camadas (Layered Architecture) com separação clara de responsabilidades:

```
┌─────────────────────────────────────────────────────┐
│                 Interface Web                        │
│            (facial.html + CSS + JS)                  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│              Controller Layer                        │
│           (FacialPfController.java)                  │
│  - Validação de entrada                             │
│  - Gerenciamento de upload                          │
│  - Conversão de formatos                            │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│               Service Layer                          │
│            (FacialPfService.java)                    │
│  - Lógica de negócio                                │
│  - Integração com API SERPRO                        │
│  - Codificação Base64                               │
│  - Construção de payload                            │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│              API Externa SERPRO                      │
│          gateway.apiserpro.serpro.gov.br            │
│         /datavalid-demonstracao/v4/pf-facial        │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│           Exception Handling Layer                   │
│       (GlobalExceptionHandler.java)                  │
│  - Captura de exceções                              │
│  - Mapeamento para HTTP status                      │
│  - Formatação de respostas de erro                  │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Estrutura de Pacotes

```
br.gov.ce.fortaleza.fd.reconfacial
│
├── 📂 controller/
│   └── FacialPfController.java          # Endpoints REST da API
│
├── 📂 service/
│   └── FacialPfService.java             # Lógica de integração com SERPRO
│
├── 📂 model/
│   ├── FacialPfRequestDto.java          # DTO de requisição
│   ├── FacialPfResponse.java            # DTO de resposta
│   └── DataValidErrorResponse.java      # DTO de erro
│
├── 📂 exception/
│   ├── DataValidException.java          # Exceção base
│   ├── DataValidExceptionFactory.java   # Factory de exceções
│   ├── DataValidErrorCode.java          # Enum com códigos DV001-DV173
│   ├── GlobalExceptionHandler.java      # Handler global de exceções
│   │
│   └── 📂 http/
│       ├── BadRequestException.java            # HTTP 400
│       ├── UnauthorizedException.java          # HTTP 401
│       ├── ForbiddenException.java             # HTTP 403
│       ├── NotFoundException.java              # HTTP 404
│       ├── RequestTooLargeException.java       # HTTP 413
│       ├── UnprocessableEntityException.java   # HTTP 422
│       ├── TooManyRequestsException.java       # HTTP 429
│       ├── InternalServerErrorException.java   # HTTP 500
│       ├── BadGatewayException.java            # HTTP 502
│       ├── ServiceUnavailableException.java    # HTTP 503
│       └── GatewayTimeoutException.java        # HTTP 504
│
├── ReconFacialApplication.java          # Classe principal Spring Boot
└── ServletInitializer.java              # Inicializador de servlet
```

### 📂 Recursos (src/main/resources/)

```
resources/
│
├── application.properties               # Configurações da aplicação
│
└── static/
    └── facial.html                      # Interface web de validação
```

---

## ⚙️ Funcionalidades

### 1. Validação Facial com Upload de Foto
- **Endpoint**: `POST /api/datavalid/facial/pf`
- **Content-Type**: `multipart/form-data`
- **Parâmetros**:
  - `cpf`: CPF da pessoa (apenas números ou formatado)
  - `photo`: Arquivo de imagem (JPG/PNG)
- **Processamento**:
  1. Validação do CPF (formato e 11 dígitos)
  2. Criação de arquivo temporário para a foto
  3. Conversão da imagem para Base64
  4. Construção do payload JSON
  5. Envio para API SERPRO
  6. Conversão de similaridade (0-1 → percentual)
  7. Classificação por faixas de probabilidade
  8. Limpeza automática de arquivos temporários

### 2. Validação Facial via JSON
- **Endpoint**: `POST /api/datavalid/facial/pf/json`
- **Content-Type**: `application/json`
- **Payload**:
  ```json
  {
    "cpf": "25774435016",
    "photoPath": "C:/path/to/photo.jpg"
  }
  ```

### 3. Classificação de Similaridade

A aplicação converte a similaridade (retornada como decimal de 0 a 1 pela API SERPRO) para percentual e classifica em faixas:

| Faixa de Similaridade | Classificação               |
|-----------------------|-----------------------------|
| 100% - 93%            | Altíssima probabilidade     |
| 92,99% - 85%          | Alta probabilidade          |
| 84,99% - 32%          | Baixa probabilidade         |
| 31,99% - 0%           | Baixíssima probabilidade    |

**Exemplo**: 
- API retorna: `"similaridade": 0.9953249118185861`
- Aplicação converte: `99%`
- Classificação: `"Altíssima probabilidade"`

### 4. Interface Web Moderna

Interface HTML/CSS/JavaScript com:
- 📸 Preview da foto em formato circular
- 📊 Indicador visual de probabilidade (gráfico circular)
- ✅ Lista de dados validados com ícones de check
- 📱 Design responsivo
- 🔄 Feedback em tempo real (loading, sucesso, erro)

---

## 🛡️ Tratamento de Exceções

### Arquitetura de Exceções

A aplicação implementa um **sistema hierárquico de exceções** com tratamento global:

```
DataValidException (base)
    │
    ├── BadRequestException (400)
    ├── UnauthorizedException (401)
    ├── ForbiddenException (403)
    ├── NotFoundException (404)
    ├── RequestTooLargeException (413)
    ├── UnprocessableEntityException (422) ← Mais comum
    ├── TooManyRequestsException (429)
    ├── InternalServerErrorException (500)
    ├── BadGatewayException (502)
    ├── ServiceUnavailableException (503)
    └── GatewayTimeoutException (504)
```

### DataValidExceptionFactory

Factory responsável por:
1. **Parsing de erros JSON**: Extrai `code`, `message`, e `link` da resposta
2. **Criação de exceção específica**: Mapeia HTTP status para exceção apropriada
3. **Enriquecimento de mensagens**: Adiciona mensagens padrão quando necessário

### GlobalExceptionHandler

Handler global (`@RestControllerAdvice`) que:
- Intercepta todas as exceções da aplicação
- Retorna respostas JSON padronizadas:
  ```json
  {
    "timestamp": "2026-03-03T10:30:00",
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "Tamanho da imagem da face inválido",
    "code": "DV042",
    "link": "https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/"
  }
  ```
- Registra logs detalhados para rastreamento

### Códigos de Erro DataValid (DataValidErrorCode)

Enum completo com **todos os 50+ códigos** da API DataValid, organizados por categoria:

#### 📋 LGPD e Requisitos Mínimos
- **DV001**: Dados de menor de idade (não validado)
- **DV002**: Dados não atendem requisitos mínimos

#### 🆔 Validação de CPF/CNPJ
- **DV010**: CPF inválido
- **DV020**: CNPJ inválido
- **DV011-DV022**: Domínios inválidos (nacionalidade, sexo, UF, etc.)

#### 👆 Impressão Digital
- **DV030-DV036**: Erros de biometria digital (posição, formato, qualidade, tamanho)

#### 🤳 Validação Facial (Principais)
- **DV040**: Imagem da face não encontrada nas bases
- **DV041**: Não foi possível reconhecer a face
- **DV042**: Tamanho inválido
- **DV043**: Imagem corrompida
- **DV044**: Erro ao recuperar Base64
- **DV045**: Qualidade baixa
- **DV046**: Mais de uma face reconhecida
- **DV047**: Formato inválido

#### 📷 Face de Referência
- **DV048-DV053**: Erros similares à validação facial para foto de referência

#### 🧬 Liveness (Prova de Vida)
- **DV061**: Baixa qualidade para vivacidade
- **DV062**: Imagem não reconhecida como real
- **DV170-DV173**: Erros de PIN e prova de vida

#### 🪪 OCR CNH
- **DV079-DV089**: Erros de leitura de documento (OCR)

#### 📱 QR Code CNH
- **DV101-DV112**: Erros de leitura de QR Code

#### 🔌 Serviços
- **DV150**: Serviço de integração facial indisponível
- **DV151**: Serviço de integração digital indisponível
- **DV152**: Serviço de integração QR Code indisponível
- **DVXX**: Algum serviço integrado está indisponível

---

## 🚀 Como Executar

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.6+**
- **Token de acesso** da API SERPRO DataValid

### Passos

1. **Clone o repositório**:
   ```bash
   git clone <url-do-repositorio>
   cd api-java-datavalid
   ```

2. **Configure o token de autenticação - Demonstração (06aef429-a981-3ec5-a1f8-71d38d86481e)**:
   
   Edite o arquivo `FacialPfController.java` e substitua o token:
   ```java
   String token = "SEU_TOKEN_AQUI";
   ```

3. **Execute a aplicação**:

   **Usando Maven Wrapper** (recomendado):
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```

   **Usando Maven instalado**:
   ```bash
   mvn spring-boot:run
   ```

4. **Acesse a aplicação**:

   - **API REST**: http://localhost:8080/api/datavalid/facial/pf
   - **Interface Web**: http://localhost:8080/facial.html

### Build para Produção

```bash
# Gerar JAR executável
./mvnw clean package

# Executar JAR
java -jar target/api-java-datavalid-0.0.1-SNAPSHOT.jar
```

---

## 🌐 Endpoints da API

### 1. POST `/api/datavalid/facial/pf` (Multipart)

Valida facial através de upload de arquivo.

**Content-Type**: `multipart/form-data`

**Parâmetros**:
| Campo  | Tipo   | Obrigatório | Descrição                    |
|--------|--------|-------------|------------------------------|
| cpf    | string | Sim         | CPF da pessoa (11 dígitos)   |
| photo  | file   | Sim         | Arquivo de imagem (JPG/PNG)  |

**Resposta de Sucesso (200)**:
```json
{
  "rfb_existe": true,
  "cnh_existe": true,
  "rfb": {
    "nome": "NOME DA PESSOA",
    "cpf": "25774435016"
  },
  "cnh": {
    "endereco": {}
  },
  "biometria_facial": {
    "vivacidade": "REAL",
    "disponivel": true,
    "probabilidade": "Altíssima probabilidade",
    "similaridade": 100
  }
}
```

**Resposta de Erro (422)**:
```json
{
  "timestamp": "2026-03-03T10:30:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Tamanho da imagem da face inválido",
  "code": "DV042",
  "link": "https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/"
}
```

### 2. POST `/api/datavalid/facial/pf/json` (JSON)

Valida facial através de caminho de arquivo local.

**Content-Type**: `application/json`

**Body**:
```json
{
  "cpf": "25774435016",
  "photoPath": "C:/path/to/photo.jpg"
}
```

**Mesma estrutura de resposta do endpoint multipart**

### 3. GET `/api/datavalid/facial/pf`

Retorna informações sobre o endpoint (útil para testes).

**Resposta**:
```json
{
  "endpoint": "/api/datavalid/facial/pf",
  "method": "POST (multipart: cpf, foto)",
  "note": "Use POST to submit CPF and foto. This GET is for testing."
}
```

---

## 🖥️ Interface Web

### Acesso
http://localhost:8080/facial.html

### Recursos

#### 📸 Upload e Preview
- Input de CPF com validação
- Upload de arquivo de foto (JPG/PNG)
- Preview automático da foto em formato circular

#### 📊 Visualização de Resultados
- **Foto circular** da pessoa com borda e sombra
- **Indicador de probabilidade** com gráfico circular preenchido baseado no percentual
- **Percentual exato** exibido no centro do gráfico
- **Classificação textual** (Altíssima, Alta, Baixa, Baixíssima probabilidade)

#### ✅ Dados Validados
- CPF
- Vivacidade (REAL/FAKE)
- Status RFB (Cadastro encontrado/Não encontrado)
- Status CNH (Cadastro encontrado/Não encontrado)
- Nome (RFB)
- Nome (CNH)
- Disponibilidade de biometria

#### 🎨 Design
- Interface moderna com cores institucionais
- Ícones de verificação em verde
- Animações suaves
- Responsivo (desktop e mobile)
- Feedback visual de loading e erros

### Tecnologias da Interface
- **HTML5**: Estrutura semântica
- **CSS3**: 
  - Flexbox e Grid para layout
  - CSS Variables para cores
  - Border-radius para elementos circulares
  - Box-shadow para profundidade
  - Transitions para animações
- **JavaScript**:
  - Fetch API para requisições assíncronas
  - FileReader API para preview de imagens
  - Template literals para renderização dinâmica
  - Console.log para debugging

---

## 💡 Exemplos de Uso

### Exemplo 1: cURL - Upload Multipart

```bash
curl -X POST http://localhost:8080/api/datavalid/facial/pf \
  -F "cpf=25774435016" \
  -F "photo=@/path/to/photo.jpg"
```

### Exemplo 2: cURL - JSON

```bash
curl -X POST http://localhost:8080/api/datavalid/facial/pf/json \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "25774435016",
    "photoPath": "C:/Users/foto.jpg"
  }'
```

### Exemplo 3: JavaScript (Fetch API)

```javascript
const formData = new FormData();
formData.append('cpf', '25774435016');
formData.append('photo', fileInput.files[0]);

fetch('/api/datavalid/facial/pf', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => {
    console.log('Similaridade:', data.biometria_facial.similaridade + '%');
    console.log('Probabilidade:', data.biometria_facial.probabilidade);
})
.catch(error => console.error('Erro:', error));
```

### Exemplo 4: Java (HttpClient)

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/datavalid/facial/pf/json"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(
        "{\"cpf\":\"25774435016\",\"photoPath\":\"C:/foto.jpg\"}"
    ))
    .build();

HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

---

## 📝 Códigos de Retorno

A aplicação trata todos os códigos HTTP e códigos DataValid conforme documentação oficial:

### HTTP Status Codes

| Código | Descrição                      | Exceção                       |
|--------|--------------------------------|-------------------------------|
| 200    | Sucesso                        | -                             |
| 400    | Requisição inválida            | BadRequestException           |
| 401    | Não autenticado                | UnauthorizedException         |
| 403    | Acesso negado                  | ForbiddenException            |
| 404    | Não encontrado                 | NotFoundException             |
| 413    | Arquivo muito grande           | RequestTooLargeException      |
| 422    | Entidade não processável       | UnprocessableEntityException  |
| 429    | Muitas requisições             | TooManyRequestsException      |
| 500    | Erro interno do servidor       | InternalServerErrorException  |
| 502    | Bad Gateway                    | BadGatewayException           |
| 503    | Serviço indisponível           | ServiceUnavailableException   |
| 504    | Gateway Timeout                | GatewayTimeoutException       |

### Referência Completa

Para lista completa de códigos DataValid (DV001-DV173), consulte:
- [Documentação Oficial SERPRO - Códigos de Retorno](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/)
- Enum `DataValidErrorCode.java` no código fonte

---

## 📊 Logs e Monitoramento

A aplicação registra logs detalhados em múltiplos pontos:

### FacialPfService
```
INFO: Resposta da API SERPRO - Status: 200 - Body: {...}
WARNING: Erro na API SERPRO - Status HTTP: 422 - Resposta: {...}
WARNING: Exceção criada: UnprocessableEntityException (HTTP 422) - Mensagem: ...
```

### GlobalExceptionHandler
```
WARNING: GlobalExceptionHandler capturou: UnprocessableEntityException | HTTP Status: 422 | ErrorCode: DV042 | Message: ...
SEVERE: Exceção não tratada: IOException | Message: ...
```

---

## 🔒 Segurança

### Recomendações

1. **Token de Autenticação**: 
   - Nunca commitar tokens em repositórios públicos
   - Usar variáveis de ambiente: `System.getenv("SERPRO_TOKEN")`
   - Rotacionar tokens periodicamente

2. **Validação de Entrada**:
   - CPF validado (11 dígitos)
   - Tipos de arquivo permitidos (JPG/PNG)
   - Tamanho máximo de arquivo

3. **Arquivos Temporários**:
   - Limpeza automática com `try-finally`
   - Diretório temporário do sistema

4. **HTTPS**:
   - Usar HTTPS em produção
   - Validar certificados SSL

---

## 📚 Documentação Adicional

- [API SERPRO DataValid - Documentação Oficial](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Jackson JSON Documentation](https://github.com/FasterXML/jackson-docs)

---

## 👥 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais e de demonstração.

---

## ✉️ Contato

**Desenvolvido por**: Victor Vasconcelos Lima & Copilot

**API SERPRO**: Para dúvidas sobre a API DataValid, consulte o [Portal de APIs do SERPRO](https://apicenter.estaleiro.serpro.gov.br/)

---

## 🙏 Agradecimentos

- SERPRO pela disponibilização da API DataValid
- Spring Framework pela excelente documentação
