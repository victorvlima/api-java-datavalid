package br.gov.ce.fortaleza.fd.datavalid.model;

public class FacialPfResponse {

    private boolean fotoExiste;
    private FaceValidationResult foto;

    public boolean getFotoExiste() {
        return fotoExiste;
    }

    public void setFotoExiste(boolean fotoExiste) {
        this.fotoExiste = fotoExiste;
    }

    public FaceValidationResult getFoto() {
        return foto;
    }

    public void setFoto(FaceValidationResult foto) {
        this.foto = foto;
    }

    public static class FaceValidationResult {
        private Double faceSimilaridade;

        public Double getFaceSimilaridade() {
            return faceSimilaridade;
        }

        public void setFaceSimilaridade(Double faceSimilaridade) {
            this.faceSimilaridade = faceSimilaridade;
        }
    }
}
