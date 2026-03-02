package br.gov.ce.fortaleza.fd.datavalid.exception;

/**
 * Enum com todos os códigos de erro da API SERPRO DataValid.
 * Referência: https://apicenter.estaleiro.serpro.gov.br/documentacao/datavalid/codigos_retorno/
 */
public enum DataValidErrorCode {
    
    // LGPD e Requisitos Mínimos
    DV001("LGPD: Dados de menor de idade - Atualmente, o Datavalid não valida dados de criança e adolescente"),
    DV002("Dados encontrados na base não atendem aos requisitos mínimos para validação"),
    
    // Validação de Dados - CPF/CNPJ
    DV010("CPF inválido - Verifique se há algo de errado ou incompleto no CPF enviado"),
    DV020("CNPJ inválido - Há algo de errado ou incompleto no CNPJ enviado"),
    
    // Validação de Dados - Domínios
    DV011("Nacionalidade inválida - O dado não faz parte da tabela de domínio"),
    DV012("Tipo do documento inválido - O dado não faz parte da tabela de domínio"),
    DV013("Sexo inválido - O dado não faz parte da tabela de domínio"),
    DV014("Situação do CPF inválido - O dado não faz parte da tabela de domínio"),
    DV016("Código da situação da CNH inválido - O dado não faz parte da tabela de domínio"),
    DV017("Descrição da situação da CNH inválida - O dado não faz parte da tabela de domínio"),
    DV018("UF inválida - O dado não faz parte da tabela de domínio"),
    DV021("Código da situação cadastral inválido - O dado não faz parte da tabela de domínio"),
    DV022("Porte inválido - O dado não faz parte da tabela de domínio"),
    
    // Impressão Digital
    DV030("Posição da impressão digital inválida"),
    DV031("Formato da imagem da impressão digital inválido"),
    DV032("Erro ao recuperar imagem da impressão digital codificada em base64"),
    DV033("Imagem da impressão digital corrompida ou fora do formato esperado"),
    DV034("Qualidade baixa da imagem da impressão digital"),
    DV035("Tamanho da imagem da impressão digital inválido"),
    DV036("Posição da impressão digital duplicada"),
    
    // Validação Facial
    DV040("Imagem da face não encontrada nas bases - O CPF não possui cadastro de imagem da face"),
    DV041("Não foi possível reconhecer a face na imagem"),
    DV042("Tamanho da imagem da face inválido"),
    DV043("Imagem da face corrompida ou fora do formato esperado"),
    DV044("Erro ao recuperar imagem da face codificada em base64"),
    DV045("Qualidade baixa da imagem da face"),
    DV046("Foi reconhecido mais de uma face na imagem"),
    DV047("Formato da imagem da face inválido"),
    
    // Face de Referência
    DV048("Erro ao recuperar imagem da face de referência codificada em base64"),
    DV049("Imagem da face de referência corrompida ou fora do formato esperado"),
    DV050("Tamanho da imagem da face de referência inválido"),
    DV051("Não foi possível reconhecer a face na imagem de face de referência"),
    DV052("Qualidade baixa da imagem da face de referência"),
    DV053("Foi reconhecida mais de uma face na imagem da face de referência"),
    
    // Liveness (Prova de Vida)
    DV061("Baixa qualidade da imagem da face para checagem de vivacidade (liveness)"),
    DV062("Imagem da face não foi reconhecida como real na checagem de vivacidade (liveness)"),
    DV170("PIN não encontrado"),
    DV171("Prova de vida ainda não realizada pelo usuário"),
    DV172("PIN expirado"),
    DV173("Quantidade de tentativas excedida"),
    
    // OCR da CNH
    DV079("Documento obrigatório - O envio da imagem da frente do documento é obrigatório"),
    DV080("Formato do documento inválido"),
    DV081("Erro ao recuperar documento codificado em base64"),
    DV082("Arquivo do documento corrompido ou fora do formato esperado"),
    DV083("Tamanho do documento inválido"),
    DV084("O documento informado não foi reconhecido como válido"),
    DV085("Foi reconhecido mais de um documento"),
    DV086("Não foi possível recuperar o CPF no documento"),
    DV087("Não foi possível recuperar a foto da face no documento"),
    DV088("O CPF informado não pertence ao CPF do documento"),
    DV089("Quantidade de campos válidos recuperados do documento não é suficiente para validação"),
    
    // QR Code da CNH
    DV101("Erro ao recuperar imagem do QR Code codificada em base64"),
    DV102("Imagem do QR Code corrompida ou fora do formato esperado"),
    DV103("Tamanho da imagem do QR Code inválido"),
    DV104("Não foi possível reconhecer o QR Code na imagem"),
    DV105("Foi reconhecido mais de um QR Code na imagem"),
    DV106("O QR Code informado não foi reconhecido como válido"),
    DV107("QR Code não pertence a uma CNH"),
    DV108("Imagem do QR Code não é de uma CNH"),
    DV109("O CPF informado não pertence ao CPF do QR Code"),
    DV110("QR Code não pertence a uma CNH"),
    DV111("QR Code não pertence a uma CNH"),
    DV112("QR Code não pertence a uma CNH"),
    
    // Serviços de Integração
    DV150("Serviço de integração facial indisponível"),
    DV151("Serviço de integração digital indisponível"),
    DV152("Serviço de integração qrcode indisponível"),
    
    // Códigos Genéricos
    DVXX("Algum serviço integrado ao Datavalid está indisponível no momento"),
    UNKNOWN("Código de erro desconhecido");
    
    private final String description;
    
    DataValidErrorCode(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtém o enum do código de erro a partir de uma string.
     * @param code código do erro (ex: "DV042")
     * @return o enum correspondente ou UNKNOWN se não encontrado
     */
    public static DataValidErrorCode fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN;
        }
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
