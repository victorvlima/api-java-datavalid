
package br.gov.ce.fortaleza.fd.datavalid.controller;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfRequestDto;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/datavalid/facial/pf")
public class FacialPfController {

	/**
	 * Minimal endpoint to "validate" a face by CPF and image.
	 * This implementation performs basic parameter validation and returns
	 * a mock success response. Replace the mock behavior with real
	 * integration to DataValid services when available.
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateByCpf(
			@RequestParam("cpf") String cpf,
			@RequestParam("photo") MultipartFile photo
	) throws IOException {

		if (cpf == null || cpf.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cpf is required");
		}

		String digits = cpf.replaceAll("\\D", "");
		if (digits.length() != 11) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cpf must contain 11 digits");
		}

		if (photo == null || photo.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("photo is required");
		}

		// Minimal behavior: return a mock response indicating a found face
		FacialPfResponse response = new FacialPfResponse();
		response.setFotoExiste(true);

		FacialPfResponse.FaceValidationResult result = new FacialPfResponse.FaceValidationResult();
		// fixed similarity for the minimal implementation
		result.setFaceSimilaridade(0.92);
		response.setFoto(result);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<?> info() {
		Map<String, Object> info = new HashMap<>();
		info.put("endpoint", "/api/datavalid/facial/pf");
		info.put("method", "POST (multipart: cpf, photo)");
		info.put("note", "Use POST to submit CPF and photo. This GET is for testing.");
		return ResponseEntity.ok(info);
	}

	/**
	 * Novo endpoint: aceita JSON { cpf, photoPath } e simula validação facial.
	 */
	@PostMapping(path = "/json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateByCpfJson(@org.springframework.web.bind.annotation.RequestBody FacialPfRequestDto request) throws IOException {
		if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cpf is required");
		}
		String digits = request.getCpf().replaceAll("\\D", "");
		if (digits.length() != 11) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cpf must contain 11 digits");
		}
		if (request.getPhotoPath() == null || request.getPhotoPath().trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("photoPath is required");
		}
		java.io.File file = new java.io.File(request.getPhotoPath());
		if (!file.exists() || !file.isFile()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("photoPath does not exist or is not a file");
		}
		// Aqui poderia ler o arquivo, mas para manter simples só simula
		FacialPfResponse response = new FacialPfResponse();
		response.setFotoExiste(true);
		FacialPfResponse.FaceValidationResult result = new FacialPfResponse.FaceValidationResult();
		result.setFaceSimilaridade(0.92);
		response.setFoto(result);
		return ResponseEntity.ok(response);
	}
}
