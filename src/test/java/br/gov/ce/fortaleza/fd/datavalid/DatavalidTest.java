package br.gov.ce.fortaleza.fd.datavalid;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import br.gov.ce.fortaleza.fd.datavalid.service.FacialValidationService;

public class DatavalidTest {

    @Autowired
    private FacialValidationService facialService;

    @Test
    public void testFacialValidation() {
        // Validar CPF + Foto (Base64)
        //FacialPfResponse response = facialService.validateFacialWithCpf(
        //    "25774435016",
        //    "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
        //);

        // Validar CPF + Foto (Arquivo)
        FacialPfResponse response = new FacialPfResponse();
        try {
            response = facialService.validateFacialWithFile(
                "25774435016",
                new File("/img/foto.jpg")
            );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Verificar resultado
        if (response != null && facialService.validateSimilarityThreshold(response)) {
            System.out.println("✅ Usuário validado!");
            Double similarity = facialService.getSimilarityPercentage(response);
            System.out.println("Similaridade: " + similarity + "%");
        }
    }
}
