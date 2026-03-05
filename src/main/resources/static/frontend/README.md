# 📸 Aplicação de Captura de Imagem Profissional

Aplicação web completa para captura de imagens com webcam, processamento automático e padronização de saída.

## ✨ Funcionalidades

- 🎥 **Acesso à Webcam**: Utiliza a API `getUserMedia` para acessar a câmera do dispositivo
- 🎯 **Guia Visual**: Overlay com círculo guia para enquadramento perfeito do rosto
- 📐 **Padronização Automática**: 
  - Proporção 3x4
  - Resolução 600x600 pixels
  - Formato JPG com 90% de qualidade
- 🔄 **Tentar Novamente**: Refazer a captura quando necessário
- ⬇️ **Download**: Baixar a imagem processada

## 🚀 Como Usar

### Método 1: Live Server (Recomendado)

1. Instale a extensão **Live Server** no VS Code
2. Clique com o botão direito no arquivo `index.html`
3. Selecione "Open with Live Server"
4. A aplicação abrirá automaticamente no navegador

### Método 2: Servidor Python

```bash
# Python 3
python -m http.server 8000

# Acesse: http://localhost:8000
```

### Método 3: Servidor Node.js

```bash
# Instale o http-server globalmente
npm install -g http-server

# Execute no diretório
http-server -p 8000

# Acesse: http://localhost:8000
```

### Método 4: Extensão PHP

```bash
php -S localhost:8000
```

## 📋 Requisitos

- **Navegador moderno** com suporte a:
  - `getUserMedia` API
  - `Canvas` API
  - ES6+ JavaScript
- **Webcam** conectada e funcional
- **HTTPS ou localhost** (requisito de segurança para acesso à câmera)

### Navegadores Suportados

- ✅ Chrome 53+
- ✅ Firefox 36+
- ✅ Edge 79+
- ✅ Safari 11+
- ✅ Opera 40+

## 🎨 Estrutura do Projeto

```
frontend/
├── index.html      # Estrutura HTML da aplicação
├── style.css       # Estilos e responsividade
├── script.js       # Lógica de captura e processamento
└── README.md       # Documentação
```

## 🔧 Especificações Técnicas

### Processamento de Imagem

1. **Captura**: Frame atual do vídeo desenhado em canvas temporário
2. **Crop**: Recorte centralizado para manter proporção 3:4
3. **Redimensionamento**: Aplicado com `imageSmoothingQuality = 'high'`
4. **Conversão**: JPG com qualidade de 90% via `toDataURL`

### Configurações (script.js)

```javascript
const CONFIG = {
    targetWidth: 600,      // Largura final
    targetHeight: 600,     // Altura final
    aspectRatio: 3 / 4,    // Proporção 3:4
    jpegQuality: 0.9,      // Qualidade JPG (90%)
    fileName: 'foto-capturada.jpg'
};
```

## 🎯 Fluxo de Uso

1. **Acesso à Câmera**: Ao carregar, solicita permissão
2. **Enquadramento**: Posicione o rosto no círculo guia
3. **Captura**: Clique em "Capturar Foto"
4. **Preview**: Visualize a imagem processada
5. **Ações**:
   - **Tentar Novamente**: Retorna à captura
   - **Download**: Baixa a imagem em JPG

## 🛡️ Segurança

- ⚠️ **HTTPS Obrigatório**: Navegadores modernos exigem HTTPS ou localhost para acesso à câmera
- 🔒 **Permissões**: Usuário deve autorizar explicitamente o uso da webcam
- 🚫 **Sem Armazenamento**: Nenhuma imagem é enviada ou armazenada em servidor

## 🎨 Personalização

### Alterar Formato do Guia

Em `style.css`, modifique `.guide-circle`:

```css
/* Circular (padrão) */
.guide-circle {
    border-radius: 50%;
}

/* Retangular */
.guide-circle {
    border-radius: 10px;
}

/* Quadrado */
.guide-circle {
    width: 300px;
    height: 300px;
    border-radius: 10px;
}
```

### Alterar Resolução

Em `script.js`:

```javascript
const CONFIG = {
    targetWidth: 800,   // Nova largura
    targetHeight: 800,  // Nova altura
    // ...
};
```

## 📱 Responsividade

- ✅ Design adaptativo para mobile e desktop
- ✅ Botões empilhados em telas pequenas
- ✅ Guia visual proporcional ao viewport

## ♿ Acessibilidade

- ✅ Suporte a `prefers-reduced-motion`
- ✅ Estados de foco visíveis nos botões
- ✅ Feedback visual e textual de status
- ✅ Ícones descritivos

## 🐛 Solução de Problemas

### Câmera não funciona

- Verifique se está usando HTTPS ou localhost
- Confirme que a câmera não está sendo usada por outro aplicativo
- Teste as permissões do navegador

### Imagem distorcida

- A aplicação mantém automaticamente a proporção 3:4
- O crop é centralizado para melhor enquadramento

### Download não funciona

- Verifique as configurações de pop-up do navegador
- Confirme permissões de download

## 📄 Licença

Projeto desenvolvido para fins educacionais e profissionais.

## 🤝 Contribuições

Sugestões e melhorias são bem-vindas!

---

**Desenvolvido com HTML5, CSS3 e JavaScript Vanilla** 🚀
