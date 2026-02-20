package br.gov.ce.fortaleza.fd.datavalid.example;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPf;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import br.gov.ce.fortaleza.fd.datavalid.service.FacialValidationService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Example usage of facial validation with CPF
 * 
 * Demonstrates:
 * 1. Validating CPF with facial photo (base64)
 * 2. Validating CPF with facial photo (from file)
 * 3. Handling similarity threshold
 * 4. Error handling and validation
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
@Component
public class FacialValidationExample {

    private final FacialValidationService facialValidationService;

    public FacialValidationExample(FacialValidationService facialValidationService) {
        this.facialValidationService = facialValidationService;
    }

    /**
     * Example 1: Validate CPF with base64 face image
     * 
     * @param cpf CPF number
     * @param photoBase64 Face image in base64 format
     * @return Validation result or null if failed
     */
    public FacialPfResponse validateFacialWithCpfBase64(String cpf, String photoBase64) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  Exemplo 1: Validação Facial com Base64                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            System.out.println("CPF: " + cpf);
            System.out.println("Foto: " + (photoBase64 != null ? photoBase64.substring(0, Math.min(50, photoBase64.length())) + "..." : "null"));

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                    cpf,
                    photoBase64
            );

            // Display results
            if (response != null) {
                System.out.println("\n✓ Validação completada com sucesso!");
                System.out.println("  Foto existe: " + response.getFotoExiste());
                
                Double similarity = facialValidationService.getSimilarityPercentage(response);
                if (similarity != null) {
                    System.out.println("  Similaridade: " + String.format("%.2f%%", similarity));
                }

                boolean meetsThreshold = facialValidationService.validateSimilarityThreshold(response);
                System.out.println("  Atende threshold (85%): " + (meetsThreshold ? "SIM" : "NÃO"));

                return response;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Erro na requisição: " + e.getMessage());
        }

        return null;
    }

    /**
     * Example 2: Validate CPF with face image from file
     * 
     * @param cpf CPF number
     * @param photoFilePath Path to photo file (JPG, PNG, or PDF)
     * @return Validation result or null if failed
     */
    public FacialPfResponse validateFacialWithCpfFile(String cpf, String photoFilePath) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  Exemplo 2: Validação Facial com Arquivo                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            System.out.println("CPF: " + cpf);
            System.out.println("Arquivo: " + photoFilePath);

            File photoFile = new File(photoFilePath);
            
            // Validate file exists
            if (!photoFile.exists()) {
                System.err.println("✗ Arquivo não encontrado: " + photoFilePath);
                return null;
            }

            System.out.println("Tamanho do arquivo: " + (photoFile.length() / 1024) + " KB");

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithFile(
                    cpf,
                    photoFile
            );

            // Display results
            if (response != null) {
                System.out.println("\n✓ Validação completada com sucesso!");
                System.out.println("  Foto existe: " + response.getFotoExiste());
                
                Double similarity = facialValidationService.getSimilarityPercentage(response);
                if (similarity != null) {
                    System.out.println("  Similaridade: " + String.format("%.2f%%", similarity));
                }

                boolean meetsThreshold = facialValidationService.validateSimilarityThreshold(response);
                System.out.println("  Atende threshold (85%): " + (meetsThreshold ? "SIM" : "NÃO"));

                return response;
            }

        } catch (IOException e) {
            System.err.println("✗ Erro ao ler arquivo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Erro na requisição: " + e.getMessage());
        }

        return null;
    }

    /**
     * Example 3: Validate CPF with additional validation data
     * 
     * @param cpf CPF number
     * @param photoBase64 Face image base64
     * @param nome Name for validation
     * @param dataNascimento Birth date (YYYY-MM-DD)
     * @return Validation result or null if failed
     */
    public FacialPfResponse validateFacialWithValidationData(
            String cpf,
            String photoBase64,
            String nome,
            String dataNascimento) {
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  Exemplo 3: Validação Facial com Dados de Validação        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            System.out.println("CPF: " + cpf);
            System.out.println("Nome: " + nome);
            System.out.println("Data de Nascimento: " + dataNascimento);

            // Create validation data
            FacialPf.ValidationData validationData = new FacialPf.ValidationData(
                    nome,
                    dataNascimento
            );
            validationData.setSituacaoCpf("regular");
            validationData.setNacionalidade(1); // Brazilian

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                    cpf,
                    photoBase64,
                    validationData
            );

            // Display results
            if (response != null) {
                System.out.println("\n✓ Validação completada com sucesso!");
                System.out.println("  Foto existe: " + response.getFotoExiste());
                
                Double similarity = facialValidationService.getSimilarityPercentage(response);
                if (similarity != null) {
                    System.out.println("  Similaridade da face: " + String.format("%.2f%%", similarity));
                }

                // Display CPF validation results if available
                if (response.getRfb() != null) {
                    System.out.println("\n  Validação CPF/RFB:");
                    System.out.println("    Nome válido: " + response.getRfb().getNome());
                    System.out.println("    Situação CPF válida: " + response.getRfb().getSituacaoCpf());
                    System.out.println("    Data de nascimento válida: " + response.getRfb().getDataNascimento());
                }

                boolean meetsThreshold = facialValidationService.validateSimilarityThreshold(response);
                System.out.println("\n  Atende threshold (85%): " + (meetsThreshold ? "SIM" : "NÃO"));

                return response;
            }

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Erro na requisição: " + e.getMessage());
        }

        return null;
    }

    /**
     * Example 4: Check similarity threshold with custom value
     * 
     * @param cpf CPF number
     * @param photoBase64 Face image base64
     * @param customThreshold Custom threshold (0.0 to 1.0)
     * @return true if meets threshold, false otherwise
     */
    public boolean validateWithCustomThreshold(
            String cpf,
            String photoBase64,
            Double customThreshold) {
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  Exemplo 4: Validação com Threshold Customizado             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            System.out.println("CPF: " + cpf);
            System.out.println("Threshold customizado: " + String.format("%.2f%%", customThreshold * 100));

            // Perform validation
            FacialPfResponse response = facialValidationService.validateFacialWithCpf(
                    cpf,
                    photoBase64
            );

            if (response != null) {
                Double similarity = facialValidationService.getSimilarityPercentage(response);
                boolean meets = facialValidationService.validateSimilarityThreshold(response, customThreshold);
                
                System.out.println("\n✓ Resultado:");
                System.out.println("  Similaridade: " + String.format("%.2f%%", similarity));
                System.out.println("  Atende threshold " + String.format("%.0f%%", customThreshold * 100) + ": " + 
                                   (meets ? "SIM" : "NÃO"));

                return meets;
            }

        } catch (Exception e) {
            System.err.println("✗ Erro: " + e.getMessage());
        }

        return false;
    }

    /**
     * Print usage guide
     */
    public static void printUsageGuide() {
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║        DataValid - Validação Facial com CPF (v4)             ║
            ╚══════════════════════════════════════════════════════════════╝
            
            REQUISITOS DE IMAGEM:
            ────────────────────
            • Formatos: JPG, PNG, PDF
            • Resolução mínima: 250x250 pixels (área do rosto)
            • Resolução recomendada: 750x750 pixels
            • Tamanho máximo: 3MB (total da requisição)
            • Padrão de qualidade: ICAO
            
            EXEMPLOS DE USO:
            ───────────────
            
            1. Validar com base64:
               FacialPfResponse result = example.validateFacialWithCpfBase64(
                   "12345678901",
                   "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
               );
            
            2. Validar com arquivo:
               FacialPfResponse result = example.validateFacialWithCpfFile(
                   "12345678901",
                   "/caminho/para/foto.jpg"
               );
            
            3. Validar com dados adicionais:
               FacialPfResponse result = example.validateFacialWithValidationData(
                   "12345678901",
                   "iVBORw0KGgoAAAANSUhEUgAAAAUA...",
                   "João Silva",
                   "1990-01-15"
               );
            
            4. Validar com threshold customizado:
               boolean meetsThreshold = example.validateWithCustomThreshold(
                   "12345678901",
                   "iVBORw0KGgoAAAANSUhEUgAAAAUA...",
                   0.90  // 90% de similaridade
               );
            
            RESPOSTA DA VALIDAÇÃO:
            ────────────────────
            {
              "foto_existe": true,
              "foto": {
                "face_similaridade": 0.95
              }
            }
            
            ENDPOINTS REST:
            ───────────────
            • POST /api/datavalid/facial/validate-with-base64
              Request: { "cpf": "...", "foto": { "imagem": "..." } }
            
            • POST /api/datavalid/facial/validate-with-file
              Params: cpf={cpf}, filePath={path}
            
            • GET /api/datavalid/facial/health
              Status do serviço
            
            LIMITES ACEITÁVEIS:
            ──────────────────
            • Similaridade >= 0.85 (85%): Aceitar
            • Similaridade 0.75-0.84: Análise manual recomendada
            • Similaridade < 0.75: Rejeitar
            
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}
