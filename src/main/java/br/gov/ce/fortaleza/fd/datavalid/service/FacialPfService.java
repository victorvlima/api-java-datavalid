package br.gov.ce.fortaleza.fd.datavalid.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidException;
import br.gov.ce.fortaleza.fd.datavalid.exception.DataValidExceptionFactory;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;

@Service
public class FacialPfService {

	//private static final String ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/datavalid/v4/pf-facial";
	private static final String ENDPOINT = "https://gateway.apiserpro.serpro.gov.br/datavalid-demonstracao/v4/pf-facial";

	/**
	 * Valida facial via SERPRO DataValid.
	 * @param cpf CPF da pessoa
	 * @param photoPath caminho da foto
	 * @param token Bearer token de autenticação
	 * @return FacialPfResponse com os dados da validação
	 * @throws DataValidException em caso de erro na API
	 */
	public FacialPfResponse validateFacial(String cpf, String photoPath, String token) throws DataValidException {
		String digits = cpf.replaceAll("\\D", "");
		if (digits.length() != 11) {
			throw new IllegalArgumentException("CPF inválido: " + cpf);
		}

		Path path = Path.of(photoPath);
		if (!Files.exists(path)) {
			throw new IllegalArgumentException("Arquivo de foto não encontrado: " + photoPath);
		}

		try {
			byte[] imageBytes = Files.readAllBytes(path);

			// Detecta o tipo da imagem para o prefixo correto
			String fileName = path.getFileName().toString().toLowerCase();
			String prefix;
			if (fileName.endsWith(".png")) {
				prefix = "data:image/png;base64,";
			} else {
				prefix = "data:image/jpg;base64,";
			}
			
			// Remove quebras de linha do base64
			String base64Raw = Base64.getEncoder().encodeToString(imageBytes).replaceAll("\r|\n", "");
			String base64 = prefix + base64Raw;

			// Monta objeto biometria_facial exatamente conforme exemplo
			Map<String, Object> biometriaFacial = new LinkedHashMap<>();
			biometriaFacial.put("formato", fileName.endsWith(".png") ? "PNG" : "JPG");
			biometriaFacial.put("vivacidade", false); // false para imagens estáticas capturadas
			biometriaFacial.put("base64", base64Raw);

			// Monta objeto validacao com endereco e cnh vazios (LinkedHashMap para garantir ordem)
			Map<String, Object> validacao = new LinkedHashMap<>();
			validacao.put("endereco", new LinkedHashMap<>()); // vazio
			validacao.put("cnh", new LinkedHashMap<>()); // vazio
			validacao.put("biometria_facial", biometriaFacial);

			// Monta payload final (LinkedHashMap para garantir ordem)
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("cpf", digits);
			payload.put("validacao", validacao);

			// Serializa o payload para JSON
			ObjectMapper mapper = new ObjectMapper();
			String jsonPayload = mapper.writeValueAsString(payload);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(ENDPOINT))
					.header("Authorization", "Bearer " + token)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> responseHttp = client.send(request, HttpResponse.BodyHandlers.ofString());
			String responseBody = responseHttp.body();
			int statusCode = responseHttp.statusCode();

			// Log da resposta completa para debug
			Logger.getLogger(FacialPfService.class.getName()).info(
				String.format("Resposta da API SERPRO - Status: %d - Body: %s", statusCode, responseBody));

			// Verifica se a requisição foi bem-sucedida
			if (!DataValidExceptionFactory.isSuccessStatus(statusCode)) {
				Logger.getLogger(FacialPfService.class.getName()).warning(
					String.format("Erro na API SERPRO - Status: %d - Resposta: %s", statusCode, responseBody));
				
				// Lança exceção específica baseada no erro
				throw DataValidExceptionFactory.createException(statusCode, responseBody);
			}

			// Desserializa resposta de sucesso
			try {
				FacialPfResponse response = mapper.readValue(responseBody, FacialPfResponse.class);
				Logger.getLogger(FacialPfService.class.getName()).info("Resposta desserializada com sucesso");
				return response;
			} catch (Exception e) {
				Logger.getLogger(FacialPfService.class.getName()).severe(
					"Falha ao desserializar resposta de sucesso: " + e.getMessage());
				throw new DataValidException("Erro ao processar resposta da API: " + e.getMessage(), e);
			}
			
		} catch (DataValidException e) {
			// Re-lança exceções da API
			throw e;
		} catch (IllegalArgumentException e) {
			// Re-lança exceções de validação de parâmetros
			throw e;
		} catch (Exception e) {
			// Encapsula outras exceções
			Logger.getLogger(FacialPfService.class.getName()).severe(
				"[ERRO] Erro inesperado na chamada ao SERPRO: " + e.getMessage());
			throw new DataValidException("Erro inesperado ao chamar API SERPRO: " + e.getMessage(), e);
		}
	}
}
