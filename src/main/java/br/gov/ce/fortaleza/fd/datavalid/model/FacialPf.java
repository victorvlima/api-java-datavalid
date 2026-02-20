package br.gov.ce.fortaleza.fd.datavalid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Model for facial validation of a Natural Person (Pessoa Física)
 * Represents the request to validate CPF and facial biometry against government databases
 * 
 * Used in endpoint: POST /v4/pf-facial
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
public class FacialPf implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * CPF number (required)
     * Format: digits only (11 characters)
     * Example: "12345678901"
     */
    @JsonProperty("cpf")
    private String cpf;

    /**
     * Facial biometry data containing the selfie/face image
     */
    @JsonProperty("foto")
    private FacialData foto;

    /**
     * Optional validation data for cross-checking
     * Can be used to validate name, birthdate, etc. against registered data
     */
    @JsonProperty("validacao")
    private ValidationData validacao;

    // Constructors
    public FacialPf() {}

    public FacialPf(String cpf, FacialData foto) {
        this.cpf = cpf;
        this.foto = foto;
    }

    public FacialPf(String cpf, FacialData foto, ValidationData validacao) {
        this.cpf = cpf;
        this.foto = foto;
        this.validacao = validacao;
    }

    // Getters and Setters
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public FacialData getFoto() {
        return foto;
    }

    public void setFoto(FacialData foto) {
        this.foto = foto;
    }

    public ValidationData getValidacao() {
        return validacao;
    }

    public void setValidacao(ValidationData validacao) {
        this.validacao = validacao;
    }

    @Override
    public String toString() {
        return "FacialPf{" +
                "cpf='" + cpf + '\'' +
                ", foto=" + foto +
                ", validacao=" + validacao +
                '}';
    }

    // ==================== Inner Classes ====================

    /**
     * Facial biometry data
     * Contains the face image in base64 format
     */
    public static class FacialData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Face image in base64 format
         * 
         * Format Requirements:
         * - JPG, PNG or PDF format
         * - Minimum resolution: 250x250 pixels (face area)
         * - Recommended resolution: 750x750 pixels
         * - Maximum file size: 3MB (total request size cannot exceed 3MB)
         * - Must follow ICAO standards for quality
         */
        @JsonProperty("imagem")
        private String imagem;

        // Constructor
        public FacialData() {}

        public FacialData(String imagem) {
            this.imagem = imagem;
        }

        // Getters and Setters
        public String getImagem() {
            return imagem;
        }

        public void setImagem(String imagem) {
            this.imagem = imagem;
        }

        @Override
        public String toString() {
            return "FacialData{" +
                    "imagem='" + (imagem != null ? imagem.substring(0, Math.min(50, imagem.length())) + "..." : null) + '\'' +
                    '}';
        }
    }

    /**
     * Optional validation data for cross-checking facial recognition
     * Can include personal data to validate against registered information
     */
    public static class ValidationData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Full name for validation
         */
        @JsonProperty("nome")
        private String nome;

        /**
         * Birth date in format: YYYY-MM-DD
         * Example: "1990-01-15"
         */
        @JsonProperty("data_nascimento")
        private String dataNascimento;

        /**
         * CPF situation status
         * Possible values: "regular", "cancelada", "nula", "suspensa"
         */
        @JsonProperty("situacao_cpf")
        private String situacaoCpf;

        /**
         * Gender
         * Possible values: "M" (Masculino), "F" (Feminino)
         */
        @JsonProperty("sexo")
        private String sexo;

        /**
         * Nationality code (IBGE)
         * 1 = Brazilian
         */
        @JsonProperty("nacionalidade")
        private Integer nacionalidade;

        // Constructors
        public ValidationData() {}

        public ValidationData(String nome, String dataNascimento) {
            this.nome = nome;
            this.dataNascimento = dataNascimento;
        }

        // Getters and Setters
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(String dataNascimento) {
            this.dataNascimento = dataNascimento;
        }

        public String getSituacaoCpf() {
            return situacaoCpf;
        }

        public void setSituacaoCpf(String situacaoCpf) {
            this.situacaoCpf = situacaoCpf;
        }

        public String getSexo() {
            return sexo;
        }

        public void setSexo(String sexo) {
            this.sexo = sexo;
        }

        public Integer getNacionalidade() {
            return nacionalidade;
        }

        public void setNacionalidade(Integer nacionalidade) {
            this.nacionalidade = nacionalidade;
        }

        @Override
        public String toString() {
            return "ValidationData{" +
                    "nome='" + nome + '\'' +
                    ", dataNascimento='" + dataNascimento + '\'' +
                    ", situacaoCpf='" + situacaoCpf + '\'' +
                    ", sexo='" + sexo + '\'' +
                    ", nacionalidade=" + nacionalidade +
                    '}';
        }
    }
}
