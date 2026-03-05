// Elementos DOM
const video = document.getElementById('video');
const canvas = document.getElementById('canvas');
const captureArea = document.getElementById('captureArea');
const previewArea = document.getElementById('previewArea');
const resultArea = document.getElementById('resultArea');
const captureBtn = document.getElementById('captureBtn');
const retryBtn = document.getElementById('retryBtn');
const validateBtn = document.getElementById('validateBtn');
const newValidationBtn = document.getElementById('newValidationBtn');
const cpfInput = document.getElementById('cpf');
const cpfDisplay = document.getElementById('cpfDisplay');
const status = document.getElementById('status');
const statusText = document.getElementById('statusText');
const resultContent = document.getElementById('resultContent');

// Configurações conforme requisitos do DataValid
// Resolução: 480x640 (proporção 3:4 - largura:altura)
// Face deve ocupar entre 50%-80% da imagem
const CONFIG = {
    targetWidth: 480,
    targetHeight: 640,
    aspectRatio: 480 / 640, // 3:4
    jpegQuality: 0.90, // 90% - melhor qualidade mantendo tamanho razoável
    apiEndpoint: 'http://localhost:8080/api/datavalid/facial/pf'
};

// Stream da câmera e imagem capturada
let stream = null;
let capturedImageBlob = null;

/**
 * Inicializa a aplicação
 */
async function init() {
    try {
        updateStatus('Solicitando acesso à câmera...', 'loading');
        await startCamera();
        updateStatus('Câmera pronta! 📹', 'ready');
    } catch (error) {
        handleError(error);
    }
}

/**
 * Inicia o stream da câmera
 */
async function startCamera() {
    try {
        const constraints = {
            video: {
                width: { ideal: 1280 },
                height: { ideal: 720 },
                facingMode: 'user'
            },
            audio: false
        };

        stream = await navigator.mediaDevices.getUserMedia(constraints);
        video.srcObject = stream;

        await new Promise((resolve) => {
            video.onloadedmetadata = () => {
                video.play();
                resolve();
            };
        });
    } catch (error) {
        if (error.name === 'NotAllowedError') {
            throw new Error('Permissão de câmera negada. Por favor, permita o acesso à câmera.');
        } else if (error.name === 'NotFoundError') {
            throw new Error('Nenhuma câmera encontrada no dispositivo.');
        } else {
            throw new Error('Erro ao acessar a câmera: ' + error.message);
        }
    }
}

/**
 * Para o stream da câmera
 */
function stopCamera() {
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
        stream = null;
    }
}

/**
 * Captura a imagem da câmera
 */
async function captureImage() {
    const cpf = cpfInput.value.trim();
    
    if (!cpf) {
        alert('Por favor, informe o CPF antes de capturar a foto.');
        cpfInput.focus();
        return;
    }

    try {
        updateStatus('Capturando imagem...', 'loading');

        const videoWidth = video.videoWidth;
        const videoHeight = video.videoHeight;

        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = videoWidth;
        tempCanvas.height = videoHeight;
        const tempCtx = tempCanvas.getContext('2d');

        tempCtx.drawImage(video, 0, 0, videoWidth, videoHeight);

        const processedImageDataUrl = processImage(tempCanvas);
        
        // Aguarda a imagem ser desenhada no canvas
        await displayProcessedImage(processedImageDataUrl);
        
        // Verifica se o canvas tem conteúdo
        const ctx = canvas.getContext('2d');
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const hasContent = imageData.data.some(value => value !== 0);
        
        if (!hasContent) {
            throw new Error('Canvas vazio após processamento da imagem');
        }
        
        // Aguarda a conversão para blob (usando Promise)
        capturedImageBlob = await new Promise((resolve) => {
            canvas.toBlob((blob) => {
                console.log('Imagem convertida para blob:', {
                    size: `${(blob.size / 1024).toFixed(2)}KB`,
                    type: blob.type,
                    dimensions: `${canvas.width}x${canvas.height}`,
                    quality: `${(CONFIG.jpegQuality * 100).toFixed(0)}%`
                });
                
                // Se a imagem ficou muito pequena, tenta com qualidade máxima
                if (blob && blob.size < 10000) { // Menor que 10KB
                    console.warn('⚠️ Imagem muito pequena, tentando com qualidade máxima...');
                    canvas.toBlob((betterBlob) => {
                        console.log('Imagem reprocessada:', {
                            size: `${(betterBlob.size / 1024).toFixed(2)}KB`,
                            quality: '100%'
                        });
                        resolve(betterBlob);
                    }, 'image/jpeg', 1.0);
                } else {
                    resolve(blob);
                }
            }, 'image/jpeg', CONFIG.jpegQuality);
        });

        if (!capturedImageBlob || capturedImageBlob.size < 1000) {
            throw new Error(`Falha ao processar a imagem. Tamanho inválido: ${capturedImageBlob ? (capturedImageBlob.size / 1024).toFixed(2) : '0'}KB`);
        }

        console.log('✅ Captura finalizada:', {
            blobSize: `${(capturedImageBlob.size / 1024).toFixed(2)}KB`,
            canvasSize: `${canvas.width}x${canvas.height}px`
        });

        stopCamera();
        captureArea.classList.add('hidden');
        previewArea.classList.remove('hidden');
        cpfDisplay.value = cpf;

        updateStatus('Imagem capturada com sucesso! ✅', 'ready');
    } catch (error) {
        handleError(error);
    }
}

/**
 * Processa a imagem para atender aos requisitos do DataValid
 * A face deve ocupar entre 50%-80% da imagem
 */
function processImage(sourceCanvas) {
    const { targetWidth, targetHeight, aspectRatio, jpegQuality } = CONFIG;

    const sourceWidth = sourceCanvas.width;
    const sourceHeight = sourceCanvas.height;

    // Zoom moderado: captura 55% da área central para zoom de ~1.8x
    // Isso garante que a face ocupe aproximadamente 60-70% da imagem final
    // Balanço ideal entre aproximação e enquadramento natural
    const zoomFactor = 0.55; // Captura 55% da área central (zoom ~1.8x)
    
    let baseCropWidth, baseCropHeight;
    const sourceAspectRatio = sourceWidth / sourceHeight;

    // Calcula dimensões base mantendo a proporção 3:4
    if (sourceAspectRatio > aspectRatio) {
        baseCropHeight = sourceHeight;
        baseCropWidth = Math.floor(baseCropHeight * aspectRatio);
    } else {
        baseCropWidth = sourceWidth;
        baseCropHeight = Math.floor(baseCropWidth / aspectRatio);
    }

    // Aplica o zoom: reduz a área de crop para focar no centro
    const cropWidth = Math.floor(baseCropWidth * zoomFactor);
    const cropHeight = Math.floor(baseCropHeight * zoomFactor);
    
    // Centraliza o crop no meio da imagem (onde o rosto deve estar)
    // Ajuste vertical: move 7% para cima para melhor enquadramento do rosto
    const cropX = Math.floor((sourceWidth - cropWidth) / 2);
    const cropY = Math.floor((sourceHeight - cropHeight) / 2) - Math.floor(cropHeight * 0.07); // 7% para cima

    console.log('📸 Processamento da imagem:', {
        original: `${sourceWidth}x${sourceHeight}`,
        crop: `${cropWidth}x${cropHeight}`,
        position: `(${cropX}, ${cropY})`,
        zoomFactor: `${(zoomFactor * 100).toFixed(0)}% da área`,
        zoomMultiplier: `${(1 / zoomFactor).toFixed(1)}x`,
        target: `${targetWidth}x${targetHeight}`,
        faceOccupancy: '~65%'
    });

    const finalCanvas = document.createElement('canvas');
    finalCanvas.width = targetWidth;
    finalCanvas.height = targetHeight;
    const finalCtx = finalCanvas.getContext('2d');

    // Melhora a qualidade do redimensionamento
    finalCtx.imageSmoothingEnabled = true;
    finalCtx.imageSmoothingQuality = 'high';

    // Preenche com branco antes de desenhar (evita transparência)
    finalCtx.fillStyle = '#FFFFFF';
    finalCtx.fillRect(0, 0, targetWidth, targetHeight);

    // Desenha a área cropada e ampliada
    finalCtx.drawImage(
        sourceCanvas,
        cropX, cropY, cropWidth, cropHeight,
        0, 0, targetWidth, targetHeight
    );

    return finalCanvas.toDataURL('image/jpeg', jpegQuality);
}

/**
 * Exibe a imagem processada no canvas
 */
function displayProcessedImage(imageDataUrl) {
    const { targetWidth, targetHeight } = CONFIG;

    canvas.width = targetWidth;
    canvas.height = targetHeight;
    const ctx = canvas.getContext('2d');

    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => {
            ctx.drawImage(img, 0, 0, targetWidth, targetHeight);
            console.log('Imagem desenhada no canvas:', {
                canvasSize: `${canvas.width}x${canvas.height}`,
                imageSize: `${img.width}x${img.height}`
            });
            resolve();
        };
        img.onerror = () => {
            reject(new Error('Falha ao carregar a imagem processada'));
        };
        img.src = imageDataUrl;
    });
}

/**
 * Reinicia o processo de captura
 */
async function retry() {
    try {
        updateStatus('Reiniciando câmera...', 'loading');

        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        capturedImageBlob = null;

        previewArea.classList.add('hidden');
        resultArea.classList.add('hidden');
        captureArea.classList.remove('hidden');

        await startCamera();
        updateStatus('Câmera pronta! 📹', 'ready');
    } catch (error) {
        handleError(error);
    }
}

/**
 * Valida a imagem com o CPF via API
 */
async function validateWithAPI() {
    const cpf = cpfDisplay.value.trim();

    if (!cpf) {
        alert('CPF não informado.');
        return;
    }

    if (!capturedImageBlob) {
        alert('Nenhuma imagem capturada. Aguarde o processamento.');
        return;
    }

    // Valida tamanho do arquivo (máximo 2MB)
    const maxSize = 2 * 1024 * 1024; // 2MB em bytes
    if (capturedImageBlob.size > maxSize) {
        alert(`Imagem muito grande (${(capturedImageBlob.size / 1024 / 1024).toFixed(2)}MB). Máximo permitido: 2MB`);
        return;
    }

    // Valida tamanho mínimo (evita imagens completamente corrompidas)
    const minSize = 1 * 1024; // 1KB mínimo
    if (capturedImageBlob.size < minSize) {
        alert(`Imagem inválida (${(capturedImageBlob.size / 1024).toFixed(2)}KB). Tente capturar novamente.`);
        return;
    }

    try {
        updateStatus('Enviando para validação...', 'loading');

        const formData = new FormData();
        formData.append('cpf', cpf);
        formData.append('photo', capturedImageBlob, 'foto-capturada.jpg');

        console.log('Enviando validação:', {
            cpf: cpf,
            imageSize: `${(capturedImageBlob.size / 1024).toFixed(2)}KB`,
            imageType: capturedImageBlob.type,
            endpoint: CONFIG.apiEndpoint
        });

        const response = await fetch(CONFIG.apiEndpoint, {
            method: 'POST',
            body: formData
        });

        const responseText = await response.text();
        console.log('Resposta da API:', response.status, responseText);

        if (response.ok) {
            const data = JSON.parse(responseText);
            displayResult(data, cpf);
            updateStatus('Validação concluída! ✅', 'ready');
        } else {
            displayError(`Erro ${response.status}: ${responseText}`);
            updateStatus('Erro na validação ❌', 'error');
        }
    } catch (error) {
        console.error('Erro na validação:', error);
        displayError(`Erro na comunicação: ${error.message}`);
        handleError(error);
    }
}

/**
 * Exibe o resultado da validação
 */
function displayResult(data, cpf) {
    console.log('Resposta da API:', data);

    let probability = 0;
    let probabilityLabel = 'Baixa probabilidade';
    let vivacidade = '';
    let disponivel = false;

    if (data.biometria_facial) {
        if (data.biometria_facial.similaridade !== undefined && data.biometria_facial.similaridade !== null) {
            probability = Math.round(data.biometria_facial.similaridade * 100);
        }

        if (probability >= 93) {
            probabilityLabel = 'Altíssima probabilidade';
        } else if (probability >= 85) {
            probabilityLabel = 'Alta probabilidade';
        } else if (probability >= 32) {
            probabilityLabel = 'Baixa probabilidade';
        } else {
            probabilityLabel = 'Baixíssima probabilidade';
        }

        vivacidade = data.biometria_facial.vivacidade || '';
        disponivel = data.biometria_facial.disponivel || false;
    }

    // Obtém a imagem do canvas
    const photoUrl = canvas.toDataURL('image/jpeg', CONFIG.jpegQuality);

    const html = `
        <div class="result-header">
            <img src="${photoUrl}" alt="Foto" class="profile-photo">
            
            <div class="probability-container">
                <div class="probability-circle" style="--probability: ${probability}">
                    <div class="probability-inner">
                        <div class="probability-value">${probability}%</div>
                    </div>
                </div>
                <div class="probability-text">${probabilityLabel}</div>
            </div>
        </div>
        
        <div class="dados-principais">
            <div class="dados-header">
                Dados da Validação
            </div>
            
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">CPF</div>
                    <div class="dado-value">${cpf}</div>
                </div>
            </div>
            
            ${vivacidade ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">Vivacidade</div>
                    <div class="dado-value">${vivacidade}</div>
                </div>
            </div>
            ` : ''}
            
            ${data.rfb_existe !== undefined ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">RFB (Receita Federal)</div>
                    <div class="dado-value">${data.rfb_existe ? 'Cadastro encontrado' : 'Não encontrado'}</div>
                </div>
            </div>
            ` : ''}
            
            ${data.cnh_existe !== undefined ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">CNH</div>
                    <div class="dado-value">${data.cnh_existe ? 'Cadastro encontrado' : 'Não encontrado'}</div>
                </div>
            </div>
            ` : ''}
            
            ${data.rfb && data.rfb.nome ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">Nome (RFB)</div>
                    <div class="dado-value">${data.rfb.nome}</div>
                </div>
            </div>
            ` : ''}
            
            ${data.cnh && data.cnh.nome ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">Nome (CNH)</div>
                    <div class="dado-value">${data.cnh.nome}</div>
                </div>
            </div>
            ` : ''}
            
            ${disponivel !== undefined ? `
            <div class="dado-item">
                <div class="check-icon"></div>
                <div class="dado-content">
                    <div class="dado-label">Biometria</div>
                    <div class="dado-value">${disponivel ? 'Disponível' : 'Não disponível'}</div>
                </div>
            </div>
            ` : ''}
        </div>
    `;

    resultContent.innerHTML = html;
    previewArea.classList.add('hidden');
    resultArea.classList.remove('hidden');
}

/**
 * Exibe mensagem de erro
 */
function displayError(message) {
    // Extrai informações do erro se for JSON
    let errorDetails = message;
    let errorCode = '';
    let errorLink = '';
    
    try {
        // Tenta extrair JSON da mensagem
        const jsonMatch = message.match(/\{.*\}/);
        if (jsonMatch) {
            const errorObj = JSON.parse(jsonMatch[0]);
            errorCode = errorObj.code || '';
            errorDetails = errorObj.message || message;
            errorLink = errorObj.link || '';
        }
    } catch (e) {
        // Se não for JSON, usa a mensagem original
    }
    
    // Mensagens específicas para erros comuns
    let helpText = '';
    if (errorCode === 'DV042') {
        helpText = `
            <div class="error-help">
                <strong>⚠️ A face não foi detectada adequadamente</strong>
                
                <div class="critical-warning">
                    🚨 SUA DISTÂNCIA DA CÂMERA ESTÁ ERRADA!
                </div>
                
                <strong style="color: #fbbf24; margin-top: 15px; display: block;">O que fazer:</strong>
                <ul>
                    <li><strong style="color: #fca5a5;">1. APROXIME-SE EXTREMAMENTE!</strong> - Chegue a 20-30cm da câmera</li>
                    <li><strong>2. Rosto deve ocupar 90% da tela</strong> - Não deve aparecer fundo ou ombros</li>
                    <li><strong>3. Iluminação FORTE</strong> - Use luz natural de dia ou várias lâmpadas</li>
                    <li><strong>4. Centralizado perfeitamente</strong> - Use o óvalo como guia exato</li>
                    <li><strong>5. Expressão neutra séria</strong> - Sem sorrir, olhar fixo na câmera</li>
                    <li><strong>6. Remova TUDO do rosto</strong> - Nem óculos de grau</li>
                    <li><strong>7. Câmera estável</strong> - Segure firme ou use suporte</li>
                </ul>
                
                <div class="info-box">
                    <strong>📏 Como saber se está correto:</strong><br>
                    • Você deve estar TÃO perto que mal consegue ver os ombros<br>
                    • O rosto ocupa quase toda a tela do vídeo<br>
                    • Distância ideal: aproximadamente a mesma que você lê um livro (20-30cm)<br>
                    • Se vê muito fundo ou espaço vazio = ESTÁ LONGE DEMAIS!
                </div>
            </div>
        `;
    }
    
    const html = `
        <div class="error-message">
            <strong>❌ Erro na Validação</strong>
            <p>${errorDetails}</p>
            ${errorCode ? `<p class="error-code">Código: ${errorCode}</p>` : ''}
            ${helpText}
            ${errorLink ? `<p><a href="${errorLink}" target="_blank">Mais informações →</a></p>` : ''}
        </div>
    `;
    
    resultContent.innerHTML = html;
    previewArea.classList.add('hidden');
    resultArea.classList.remove('hidden');
}

/**
 * Nova validação - volta para o início
 */
async function newValidation() {
    resultArea.classList.add('hidden');
    captureArea.classList.remove('hidden');
    cpfInput.value = '';
    cpfDisplay.value = '';
    capturedImageBlob = null;
    
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    await retry();
}

/**
 * Atualiza o status da aplicação
 */
function updateStatus(message, type = 'ready') {
    statusText.textContent = message;
    status.className = 'status ' + type;
}

/**
 * Trata erros
 */
function handleError(error) {
    console.error('Erro:', error);
    updateStatus('Erro: ' + error.message, 'error');
}

// Event Listeners
captureBtn.addEventListener('click', captureImage);
retryBtn.addEventListener('click', retry);
validateBtn.addEventListener('click', validateWithAPI);
newValidationBtn.addEventListener('click', newValidation);

// Formatação automática do CPF
cpfInput.addEventListener('input', (e) => {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 11) value = value.slice(0, 11);
    
    if (value.length > 9) {
        value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
        value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    } else if (value.length > 3) {
        value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
    }
    
    e.target.value = value;
});

// Inicializa quando a página carregar
window.addEventListener('load', init);
