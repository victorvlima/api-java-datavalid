
package br.gov.ce.fortaleza.fd.datavalid.controller;

import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfRequestDto;
import br.gov.ce.fortaleza.fd.datavalid.model.FacialPfResponse;
import br.gov.ce.fortaleza.fd.datavalid.service.FacialPfService;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final FacialPfService facialPfService;

	@Autowired
	public FacialPfController(FacialPfService facialPfService) {
		this.facialPfService = facialPfService;
	}

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

		// Chamada real ao serviço
		// TODO: obter token real de autenticação
		String token = "06aef429-a981-3ec5-a1f8-71d38d86481e";
		java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("upload", photo.getOriginalFilename());
		photo.transferTo(tempFile);
		FacialPfResponse response = facialPfService.validateFacial(digits, tempFile.toString(), token);
		java.nio.file.Files.deleteIfExists(tempFile);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao consultar serviço externo");
		}
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
		// Chamada real ao serviço
		// TODO: obter token real de autenticação
		String token = "06aef429-a981-3ec5-a1f8-71d38d86481e";
		FacialPfResponse response = facialPfService.validateFacial(digits, request.getPhotoPath(), token);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao consultar serviço externo");
		}
		return ResponseEntity.ok(response);
	}
}
