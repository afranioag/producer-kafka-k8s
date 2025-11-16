package com.aag.pocs.controller;

import com.aag.pocs.services.CadastroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cadastro")
@RequiredArgsConstructor
public class CadastroController {

    private final CadastroService cadastroService;

    /**
     * Endpoint para processar arquivo CSV do S3 e enviar para Kafka
     * POST /api/cadastro/processar?fileName=pessoas.csv
     */
    @PostMapping("/processar")
    public ResponseEntity<Map<String, Object>> processarArquivo(@RequestParam String fileName) {
        log.info("Request para processar arquivo: {}", fileName);

        try {
            CadastroService.ProcessamentoResult result = cadastroService.processarArquivo(fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSucesso());
            response.put("fileName", result.getFileName());
            response.put("totalRegistros", result.getTotalRegistros());
            response.put("tempoProcessamento", result.getTempoProcessamento() + "ms");

            if (!result.getSucesso()) {
                response.put("erro", result.getErro());
                return ResponseEntity.internalServerError().body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao processar request: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("erro", e.getMessage());
            errorResponse.put("fileName", fileName);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check
     * GET /api/cadastro/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cadastro-service");
        return ResponseEntity.ok(response);
    }
}