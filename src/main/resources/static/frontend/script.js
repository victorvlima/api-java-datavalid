// Elementos DOM
const video = document.getElementById('video');
const canvas = document.getElementById('canvas');
const captureArea = document.getElementById('captureArea');
const previewArea = document.getElementById('previewArea');
const captureBtn = document.getElementById('captureBtn');
const retryBtn = document.getElementById('retryBtn');
const downloadBtn = document.getElementById('downloadBtn');
const status = document.getElementById('status');
const statusText = document.getElementById('statusText');

// Configurações
const CONFIG = {
    targetWidth: 600,
    targetHeight: 600,
    aspectRatio: 3 / 4, // 3x4
    jpegQuality: 0.9, // 90% de qualidade
    fileName: 'foto-capturada.jpg'
};

// Stream da câmera
let stream = null;

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
        // Solicita acesso à câmera com configurações ideais
        const constraints = {
            video: {
                width: { ideal: 1280 },
                height: { ideal: 720 },
                facingMode: 'user' // Câmera frontal
            },
            audio: false
        };

        stream = await navigator.mediaDevices.getUserMedia(constraints);
        video.srcObject = stream;

        // Aguarda o vídeo carregar metadados
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
function captureImage() {
    try {
        updateStatus('Capturando imagem...', 'loading');

        // Obtém dimensões do vídeo
        const videoWidth = video.videoWidth;
        const videoHeight = video.videoHeight;

        // Configura o canvas temporário com as dimensões do vídeo
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = videoWidth;
        tempCanvas.height = videoHeight;
        const tempCtx = tempCanvas.getContext('2d');

        // Desenha o frame atual do vídeo no canvas temporário
        tempCtx.drawImage(video, 0, 0, videoWidth, videoHeight);

        // Processa e padroniza a imagem
        const processedImage = processImage(tempCanvas);

        // Exibe a imagem processada no canvas principal
        displayProcessedImage(processedImage);

        // Para a câmera e mostra a área de preview
        stopCamera();
        captureArea.classList.add('hidden');
        previewArea.classList.remove('hidden');

        updateStatus('Imagem capturada com sucesso! ✅', 'ready');
    } catch (error) {
        handleError(error);
    }
}

/**
 * Processa a imagem para atender aos requisitos
 * @param {HTMLCanvasElement} sourceCanvas - Canvas com a imagem original
 * @returns {string} Data URL da imagem processada
 */
function processImage(sourceCanvas) {
    const { targetWidth, targetHeight, aspectRatio, jpegQuality } = CONFIG;

    // Obtém dimensões originais
    const sourceWidth = sourceCanvas.width;
    const sourceHeight = sourceCanvas.height;

    // Calcula o crop para manter a proporção 3:4
    let cropWidth, cropHeight, cropX, cropY;

    const sourceAspectRatio = sourceWidth / sourceHeight;

    if (sourceAspectRatio > aspectRatio) {
        // Vídeo mais largo que o necessário - crop horizontal
        cropHeight = sourceHeight;
        cropWidth = Math.floor(cropHeight * aspectRatio);
        cropX = Math.floor((sourceWidth - cropWidth) / 2);
        cropY = 0;
    } else {
        // Vídeo mais alto que o necessário - crop vertical
        cropWidth = sourceWidth;
        cropHeight = Math.floor(cropWidth / aspectRatio);
        cropX = 0;
        cropY = Math.floor((sourceHeight - cropHeight) / 2);
    }

    // Cria canvas final com as dimensões alvo
    const finalCanvas = document.createElement('canvas');
    finalCanvas.width = targetWidth;
    finalCanvas.height = targetHeight;
    const finalCtx = finalCanvas.getContext('2d');

    // Melhora a qualidade do redimensionamento
    finalCtx.imageSmoothingEnabled = true;
    finalCtx.imageSmoothingQuality = 'high';

    // Desenha a imagem cropada e redimensionada
    finalCtx.drawImage(
        sourceCanvas,
        cropX, cropY, cropWidth, cropHeight, // Área de origem (crop)
        0, 0, targetWidth, targetHeight // Destino (redimensionado)
    );

    // Converte para JPG com qualidade especificada
    return finalCanvas.toDataURL('image/jpeg', jpegQuality);
}

/**
 * Exibe a imagem processada no canvas
 * @param {string} imageDataUrl - Data URL da imagem
 */
function displayProcessedImage(imageDataUrl) {
    const { targetWidth, targetHeight } = CONFIG;

    // Configura o canvas
    canvas.width = targetWidth;
    canvas.height = targetHeight;
    const ctx = canvas.getContext('2d');

    // Carrega e desenha a imagem
    const img = new Image();
    img.onload = () => {
        ctx.drawImage(img, 0, 0, targetWidth, targetHeight);
    };
    img.src = imageDataUrl;
}

/**
 * Reinicia o processo de captura
 */
async function retry() {
    try {
        updateStatus('Reiniciando câmera...', 'loading');

        // Limpa o canvas
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Volta para a área de captura
        previewArea.classList.add('hidden');
        captureArea.classList.remove('hidden');

        // Reinicia a câmera
        await startCamera();
        updateStatus('Câmera pronta! 📹', 'ready');
    } catch (error) {
        handleError(error);
    }
}

/**
 * Faz o download da imagem capturada
 */
function downloadImage() {
    try {
        updateStatus('Preparando download...', 'loading');

        // Obtém a imagem do canvas
        canvas.toBlob((blob) => {
            if (!blob) {
                throw new Error('Erro ao criar blob da imagem');
            }

            // Cria URL temporária
            const url = URL.createObjectURL(blob);

            // Cria link de download
            const link = document.createElement('a');
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
            link.download = `foto-${timestamp}.jpg`;
            link.href = url;

            // Trigger download
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            // Libera a URL
            setTimeout(() => URL.revokeObjectURL(url), 100);

            updateStatus('Download concluído! ⬇️', 'ready');
        }, 'image/jpeg', CONFIG.jpegQuality);
    } catch (error) {
        handleError(error);
    }
}

/**
 * Atualiza o status na interface
 * @param {string} message - Mensagem a exibir
 * @param {string} state - Estado: 'loading', 'ready', 'error'
 */
function updateStatus(message, state = 'loading') {
    statusText.textContent = message;
    status.className = `status ${state}`;
}

/**
 * Trata erros da aplicação
 * @param {Error} error - Erro ocorrido
 */
function handleError(error) {
    console.error('Erro:', error);
    updateStatus(`❌ ${error.message}`, 'error');

    // Se houver stream ativo, para ele
    stopCamera();

    // Exibe mensagem mais amigável
    alert(error.message);
}

/**
 * Verifica suporte do navegador
 */
function checkBrowserSupport() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Seu navegador não suporta acesso à câmera. Use um navegador moderno como Chrome, Firefox ou Edge.');
    }
}

// Event Listeners
captureBtn.addEventListener('click', captureImage);
retryBtn.addEventListener('click', retry);
downloadBtn.addEventListener('click', downloadImage);

// Cleanup ao sair da página
window.addEventListener('beforeunload', () => {
    stopCamera();
});

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    try {
        checkBrowserSupport();
        init();
    } catch (error) {
        handleError(error);
    }
});
