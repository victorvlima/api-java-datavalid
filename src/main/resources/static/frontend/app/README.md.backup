# Validação Facial - Instruções de Uso

## 📋 Requisitos para Captura da Foto

De acordo com a [documentação do DataValid](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/requisitos_face/), a foto deve atender aos seguintes requisitos:

### ✅ Requisitos Obrigatórios

1. **Iluminação**
   - Ambiente bem iluminado
   - Evitar sombras no rosto
   - Luz frontal uniforme

2. **Posicionamento**
   - Rosto centralizado na imagem
   - Olhar direto para a câmera
   - Expressão neutra
   - **IMPORTANTE**: Aproxime-se da câmera para que seu rosto preencha o óvalo guia
   - A face deve ocupar entre 50%-80% da imagem final

3. **Qualidade da Imagem**
   - Resolução: 480x640 pixels (proporção 3:4)
   - Formato: JPEG com qualidade de 95%
   - Tamanho máximo: 2MB
   - Imagem nítida (sem desfoque)

4. **Restrições**
   - Sem óculos escuros
   - Sem acessórios que cubram o rosto
   - Sem filtros ou edições
   - Apenas uma pessoa na foto

### 🚫 Erros Comuns

#### DV042 - Tamanho da imagem da face inválido

Este erro ocorre quando:
- O rosto não foi detectado corretamente
- O rosto está muito pequeno ou muito grande na imagem
- A iluminação está inadequada
- A foto está borrada ou desfocada
- Há objetos cobrindo parte do rosto

**Soluções:**
1. **🎯 Posicione-se corretamente** - Fique a 30-40cm da câmera com rosto centralizado no óvalo
2. **💡 Ilumine bem o ambiente** - Use luz natural frontal ou iluminação adequada
3. **📐 Centralize o rosto** - Use o guia oval, rosto no centro
4. **🎭 Expressão neutra** - Olhe diretamente para a câmera
5. **👓 Sem acessórios** - Remova óculos escuros, bonés ou outros itens que cubram o rosto
6. **📸 Estabilidade** - Mantenha a câmera estável durante a captura

**💡 CONFIGURAÇÃO TÉCNICA:**
O sistema utiliza zoom moderado (~1.8x) para balancear qualidade e enquadramento:
- ✅ Face deve ocupar 60-70% da imagem final
- ✅ Distância ideal: 30-40cm da câmera
- ✅ Formato enviado: **JPEG** (corrigido de JPG)
- ✅ Campo vivacidade: **false** (adequado para fotos estáticas)
- ✅ Resolução: 480x640 pixels, qualidade 90%

**🔍 Possíveis causas do erro DV042:**
1. **Formato incorreto** - Corrigido: agora envia "JPEG" ao invés de "JPG"
2. **Campo vivacidade incorreto** - Ajustado para `false` (imagens estáticas)
3. **Tamanho/posicionamento da face** - Ajuste a distância da câmera
4. **Qualidade da imagem** - Garanta boa iluminação e estabilidade
5. **Limitações do ambiente demo** - O DataValid demonstração pode ter restrições

**⚠️ NOTA:** Se o erro persistir após seguir todas as instruções, pode ser uma limitação do ambiente de demonstração do DataValid ou do CPF de teste usado.

## 🖥️ Como Usar

1. **Acesse a página**
   ```
   frontend/app/validacao.html
   ```

2. **Inicie o backend**
   ```bash
   cd backend/recon-facial
   mvn spring-boot:run
   ```

3. **Realize a validação**
   - Digite o CPF
   - Permita acesso à câmera
   - Posicione seu rosto no círculo guia
   - Clique em "Capturar Foto"
   - Clique em "Validar"

## 🔍 Debug

### Verificar logs do frontend
Abra o Console do navegador (F12) para ver:
- Tamanho da imagem capturada
📸 Processamento da imagem: {
  original: "1280x720",
  crop: "192x256",         // 40% da área
  position: "(544, 206)",
  zoomFactor: "40% da área",
  zoomMultiplier: "2.5x",  // MÁXIMO!
  target: "480x640",
  faceOccupancy: "~85%"    // Face deve ocupar ~85% da imagem capturar:**
```javascript
Processamento da imagem: {
  original: "1280x720",
  crop: "240x320",
  zoom: "50%",
  target: "480x640"
}

Imagem desenhada no canvas: {
  canvasSize: "480x640",
  imageSize: "480x640"
}

Imagem convertida para blob: {
  size: "45.23KB",        // ✅ Deve estar entre 20KB e 150KB
  type: "image/jpeg",
  dimensions: "480x640px",
  quality: "90%"
}

✅ Captura finalizada: {
  blobSize: "45.23KB",
  canvasSize: "480x640px"
}
```

### Problemas Comuns e Soluções

**1. Imagem muito pequena (< 20KB)**
- **Causa:** Câmera com baixa resolução ou ambiente muito escuro
- **Solução:** Use boa iluminação e verifique qualidade da câmera

**2. Imagem inválida (< 1KB)**
- **Causa:** Falha no processamento ou canvas vazio
- **Solução:** Recarregue a página (Ctrl+F5) e tente novamente

**3. Canvas vazio**
- **Causa:** Erro ao carregar ou processar a imagem
- **Solução:** Verifique permissões da câmera e recarregue

### Verificar logs do backend
No terminal do Spring Boot, você verá:
- Tamanho da imagem recebida
- Tamanho da imagem em base64
- Resposta da API do DataValid

## 📚 Documentação

- [DataValid - Requisitos da Face](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/requisitos_face/)
- [DataValid - Códigos de Retorno](https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/)

## 🔧 Configurações

### Ajuste do Zoom (se necessário)

Se o erro DV042 persistir, você pode ajustar o zoom aplicado na captura:

1. Abra `validacao.js`
2. Localize a função `processImage`
3. Ajuste o valor de `zoomFactor`:
   ```javascript
   const zoomFactor = 0.4;  // Valor ATUAL (40% da área = zoom 2.5x) ⭐ MÁXIMO
   // Para MENOS zoom (rosto menor): use 0.45 ou 0.5
   // ATENÇÃO: Não recomendado diminuir mais de 0.4!
   ```

**Valores disponíveis:**
- `0.4` (atual) - Zoom 2.5x, face ocupa ~85-90% da imagem ⭐ MÁXIMO RECOMENDADO
- `0.45` - Zoom 2.2x, face ocupa ~80% da imagem
- `0.5` - Zoom 2x, face ocupa ~75% da imagem

**⚠️ IMPORTANTE:** Com zoom de 2.5x, o usuário DEVE estar MUITO próximo da câmera. Se ainda ocorrer DV042, o problema não é o zoom, mas sim a posição do usuário ou qualidade da imagem.

### Frontend (validacao.js)
```javascript
const CONFIG = {
    targetWidth: 480,      // Largura da imagem final
    targetHeight: 640,     // Altura da imagem final
    aspectRatio: 480/640,  // Proporção 3:4 (largura:altura)
    jpegQuality: 0.90,     // Qualidade JPEG (90% - melhor qualidade)
    apiEndpoint: 'http://localhost:8080/api/datavalid/facial/pf'
};

// Zoom aplicado no processamento - MÁXIMO!
const zoomFactor = 0.4;    // Captura 40% da área central (zoom de 2.5x)
                           // Isso garante que a face ocupe 85-90% da imagem
                           // Ajuste vertical: 10% para cima
```

**Tamanho esperado da imagem:** Entre 20KB e 150KB (dependendo da complexidade da imagem)

### Backend
- Endpoint: `POST /api/datavalid/facial/pf`
- Content-Type: `multipart/form-data`
- Parâmetros:
  - `cpf`: CPF do cidadão (string)
  - `photo`: Arquivo da foto (file)
