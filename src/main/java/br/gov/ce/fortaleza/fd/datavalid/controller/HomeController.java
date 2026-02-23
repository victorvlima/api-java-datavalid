package br.gov.ce.fortaleza.fd.datavalid.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "api-java-datavalid");
        info.put("status", "ok");
        info.put("endpoints", new String[]{"POST /api/datavalid/auth/token", "POST /api/datavalid/facial/pf"});
        return ResponseEntity.ok(info);
    }

}
