package br.gov.ce.fortaleza.fd.datavalid.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;

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
			if (!Files.exists(path)) {
				System.err.println("Arquivo de foto não encontrado: " + photoPath);
				return null;
			}
			byte[] imageBytes = Files.readAllBytes(path);

			// Detecta o tipo da imagem para o prefixo correto
			String fileName = path.getFileName().toString().toLowerCase();
			String prefix;
			if (fileName.endsWith(".png")) {
				prefix = "data:image/png;base64,";
			} else {
				prefix = "data:image/jpeg;base64,";
			}
			// Remove quebras de linha do base64
			String base64Raw = Base64.getEncoder().encodeToString(imageBytes).replaceAll("\r|\n", "");
			String base64 = prefix + base64Raw;

			// Loga os primeiros 100 caracteres do base64 para conferência
			Logger.getLogger(FacialPfService.class.getName()).info("Base64 da imagem (início): " + base64.substring(0, Math.min(100, base64.length())));

			Map<String, Object> payload = new HashMap<>();
			payload.put("cpf", digits);
			payload.put("foto", base64);

			// Loga o payload final (atenção: pode conter dados sensíveis)
			Logger.getLogger(FacialPfService.class.getName()).info("Payload enviado ao SERPRO: {cpf: " + digits + ", foto: " + base64.substring(0, Math.min(80, base64.length())) + "...}");

			try {
				FacialPfResponse response = webClient.post()
					.uri(ENDPOINT)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header("x-signature", "1")
					.accept(MediaType.APPLICATION_JSON)
					.bodyValue(payload)
					.retrieve()
					.bodyToMono(FacialPfResponse.class)
					.block();
				return response;
			} catch (org.springframework.web.reactive.function.client.WebClientResponseException ex) {
				Logger.getLogger(FacialPfService.class.getName()).severe("Erro na chamada ao SERPRO: " + ex.getStatusCode());
				Logger.getLogger(FacialPfService.class.getName()).severe("Detalhes do erro: " + ex.toString());
				Logger.getLogger(FacialPfService.class.getName()).severe("Corpo da resposta de erro: " + ex.getResponseBodyAsString());
				return null;
			}
		} catch (Exception e) {
			// Log e retorna null
			e.printStackTrace();
			return null;
		}
	}
}
