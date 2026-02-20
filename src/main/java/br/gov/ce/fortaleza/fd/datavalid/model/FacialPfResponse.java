package br.gov.ce.fortaleza.fd.datavalid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Model for facial validation response
 * Contains the results of facial biometry validation against CPF data
 * 
 * Response from endpoint: POST /v4/pf-facial
 * 
 * @author SERPRO DataValid Integration
 * @version 1.0
 */
public class FacialPfResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Whether the face exists in the biometric databases
     */
    @JsonProperty("foto_existe")
    private Boolean fotoExiste;

    /**
     * Face validation results
     */
    @JsonProperty("foto")
    private FaceValidationResult foto;

    /**
     * Optional CPF validation results
     * If CPF validation was included in the request
     */
    @JsonProperty("rfb")
    private CpfValidationResult rfb;

    // Constructors
    public FacialPfResponse() {}

    public FacialPfResponse(Boolean fotoExiste, FaceValidationResult foto) {
        this.fotoExiste = fotoExiste;
        this.foto = foto;
    }

    // Getters and Setters
    public Boolean getFotoExiste() {
        return fotoExiste;
    }

    public void setFotoExiste(Boolean fotoExiste) {
        this.fotoExiste = fotoExiste;
    }

    public FaceValidationResult getFoto() {
        return foto;
    }

    public void setFoto(FaceValidationResult foto) {
        this.foto = foto;
    }

    public CpfValidationResult getRfb() {
        return rfb;
    }

    public void setRfb(CpfValidationResult rfb) {
        this.rfb = rfb;
    }

    @Override
    public String toString() {
        return "FacialPfResponse{" +
                "fotoExiste=" + fotoExiste +
                ", foto=" + foto +
                ", rfb=" + rfb +
                '}';
    }

    // ==================== Inner Classes ====================

    /**
     * Face validation result
     * Contains similarity score and comparison results
     */
    public static class FaceValidationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Face similarity score
         * Range: 0.0 to 1.0
         * 1.0 = Exact match
         * 0.0 = No match
         * 
         * Typical threshold for acceptance:
         * - 0.85 or higher: Strong match
         * - 0.75-0.84: Acceptable match
         * - Below 0.75: Weak or no match
         */
        @JsonProperty("face_similaridade")
        private Double faceSimilaridade;

        // Constructor
        public FaceValidationResult() {}

        public FaceValidationResult(Double faceSimilaridade) {
            this.faceSimilaridade = faceSimilaridade;
        }

        // Getters and Setters
        public Double getFaceSimilaridade() {
            return faceSimilaridade;
        }

        public void setFaceSimilaridade(Double faceSimilaridade) {
            this.faceSimilaridade = faceSimilaridade;
        }

        /**
         * Check if face similarity meets acceptance threshold
         * @param threshold Minimum similarity required (0.0 to 1.0)
         * @return true if similarity is >= threshold
         */
        public boolean meetsThreshold(Double threshold) {
            return faceSimilaridade != null && faceSimilaridade >= threshold;
        }

        /**
         * Get similarity as percentage
         * @return Similarity percentage (0-100)
         */
        public Double getSimilarityPercentage() {
            return faceSimilaridade != null ? faceSimilaridade * 100 : null;
        }

        @Override
        public String toString() {
            return "FaceValidationResult{" +
                    "faceSimilaridade=" + faceSimilaridade +
                    '}';
        }
    }

    /**
     * CPF validation result (optional)
     * Contains validation of CPF data against RFB (tax authority) database
     */
    public static class CpfValidationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Name validation result
         */
        @JsonProperty("nome")
        private Boolean nome;

        /**
         * Name similarity score (if applicable)
         * Range: 0.0 to 1.0
         */
        @JsonProperty("nome_similaridade")
        private Integer nomeSimilaridade;

        /**
         * CPF situation validation
         */
        @JsonProperty("situacao_cpf")
        private Boolean situacaoCpf;

        /**
         * Birth date validation
         */
        @JsonProperty("data_nascimento")
        private Boolean dataNascimento;

        // Constructors
        public CpfValidationResult() {}

        public CpfValidationResult(Boolean nome, Boolean situacaoCpf, Boolean dataNascimento) {
            this.nome = nome;
            this.situacaoCpf = situacaoCpf;
            this.dataNascimento = dataNascimento;
        }

        // Getters and Setters
        public Boolean getNome() {
            return nome;
        }

        public void setNome(Boolean nome) {
            this.nome = nome;
        }

        public Integer getNomeSimilaridade() {
            return nomeSimilaridade;
        }

        public void setNomeSimilaridade(Integer nomeSimilaridade) {
            this.nomeSimilaridade = nomeSimilaridade;
        }

        public Boolean getSituacaoCpf() {
            return situacaoCpf;
        }

        public void setSituacaoCpf(Boolean situacaoCpf) {
            this.situacaoCpf = situacaoCpf;
        }

        public Boolean getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(Boolean dataNascimento) {
            this.dataNascimento = dataNascimento;
        }

        @Override
        public String toString() {
            return "CpfValidationResult{" +
                    "nome=" + nome +
                    ", nomeSimilaridade=" + nomeSimilaridade +
                    ", situacaoCpf=" + situacaoCpf +
                    ", dataNascimento=" + dataNascimento +
                    '}';
        }
    }
}
