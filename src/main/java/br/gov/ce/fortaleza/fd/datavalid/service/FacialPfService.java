package br.gov.ce.fortaleza.fd.datavalid.service;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

@Service
public class FacialPfService {

	//private static final String ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/datavalid/v4/pf-facial";
	private static final String ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/datavalid-demonstracao/v4/pf-facial";

	private final WebClient webClient = WebClient.builder().build();

	/**
	 * Valida facial via SERPRO DataValid.
	 * @param cpf CPF da pessoa
	 * @param photoPath caminho da foto
	 * @param token Bearer token de autenticação
	 * @return FacialPfResponse ou null em caso de erro
	 */
	public FacialPfResponse validateFacial(String cpf, String photoPath, String token) {
		try {
			String digits = cpf.replaceAll("\\D", "");
			if (digits.length() != 11) throw new IllegalArgumentException("CPF inválido");

			Path path = Path.of(photoPath);
			byte[] imageBytes = Files.readAllBytes(path);
			String base64 = Base64.getEncoder().encodeToString(imageBytes);
            
            Map<String, Object> payload = new HashMap<>();
			payload.put("cpf", digits);
			payload.put("foto", base64);

			// Chamada HTTP
			FacialPfResponse response = webClient.post()
				.uri(ENDPOINT)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(FacialPfResponse.class)
				.block();
			return response;
		} catch (Exception e) {
			// Log e retorna null
			e.printStackTrace();
			return null;
		}
	}
}
