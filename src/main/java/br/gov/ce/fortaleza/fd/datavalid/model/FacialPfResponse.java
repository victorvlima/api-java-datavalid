package br.gov.ce.fortaleza.fd.datavalid.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacialPfResponse {

    @JsonProperty("rfb_existe")
    private Boolean rfbExiste;

    @JsonProperty("cnh_existe")
    private Boolean cnhExiste;

    @JsonProperty("rfb")
    private Map<String, Object> rfb;

    @JsonProperty("cnh")
    private CnhData cnh;

    @JsonProperty("biometria_facial")
    private BiometriaFacial biometriaFacial;

    // Getters e Setters
    public Boolean getRfbExiste() {
        return rfbExiste;
    }

    public void setRfbExiste(Boolean rfbExiste) {
        this.rfbExiste = rfbExiste;
    }

    public Boolean getCnhExiste() {
        return cnhExiste;
    }

    public void setCnhExiste(Boolean cnhExiste) {
        this.cnhExiste = cnhExiste;
    }

    public Map<String, Object> getRfb() {
        return rfb;
    }

    public void setRfb(Map<String, Object> rfb) {
        this.rfb = rfb;
    }

    public CnhData getCnh() {
        return cnh;
    }

    public void setCnh(CnhData cnh) {
        this.cnh = cnh;
    }

    public BiometriaFacial getBiometriaFacial() {
        return biometriaFacial;
    }

    public void setBiometriaFacial(BiometriaFacial biometriaFacial) {
        this.biometriaFacial = biometriaFacial;
    }

    // Classes internas para estruturar a resposta
    public static class CnhData {
        @JsonProperty("endereco")
        private Map<String, Object> endereco;

        public Map<String, Object> getEndereco() {
            return endereco;
        }

        public void setEndereco(Map<String, Object> endereco) {
            this.endereco = endereco;
        }
    }

    public static class BiometriaFacial {
        @JsonProperty("vivacidade")
        private String vivacidade;

        @JsonProperty("disponivel")
        private Boolean disponivel;

        @JsonProperty("probabilidade")
        private String probabilidade;

        @JsonProperty("similaridade")
        private Double similaridade;

        public String getVivacidade() {
            return vivacidade;
        }

        public void setVivacidade(String vivacidade) {
            this.vivacidade = vivacidade;
        }

        public Boolean getDisponivel() {
            return disponivel;
        }

        public void setDisponivel(Boolean disponivel) {
            this.disponivel = disponivel;
        }

        public String getProbabilidade() {
            return probabilidade;
        }

        public void setProbabilidade(String probabilidade) {
            this.probabilidade = probabilidade;
        }

        public Double getSimilaridade() {
            return similaridade;
        }

        public void setSimilaridade(Double similaridade) {
            this.similaridade = similaridade;
        }
    }
}
