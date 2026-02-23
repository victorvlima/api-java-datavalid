package br.gov.ce.fortaleza.fd.datavalid.model;

public class FacialPfRequestDto {
    private String cpf;
    private String photoPath;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}