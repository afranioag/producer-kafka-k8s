package com.aag.pocs.services;

import com.aag.pocs.dto.PessoaDTO;
import com.aag.pocs.producer.KafkaProducerService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class CadastroService {

    private final S3Service s3Service;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Processa arquivo CSV do S3 e envia para o Kafka
     * Retorna estatísticas do processamento
     */
    public ProcessamentoResult processarArquivo(String fileName) {
        log.info("Iniciando processamento do arquivo: {}", fileName);

        long startTime = System.currentTimeMillis();

        try {
            // 1. Ler arquivo do S3
            String csvContent = s3Service.readFileAsString(fileName);
            log.info("Arquivo lido do S3 com sucesso");

            // 2. Parsear CSV
            List<PessoaDTO> pessoas = parsearCSV(csvContent, fileName);
            log.info("CSV parseado: {} registros encontrados", pessoas.size());

            // 3. Enviar para o Kafka (assíncrono)
            CompletableFuture<Void> kafkaFuture = kafkaProducerService.sendBatch(pessoas);

            // 4. Aguardar conclusão
            kafkaFuture.join();

            long duration = System.currentTimeMillis() - startTime;

            ProcessamentoResult result = ProcessamentoResult.builder()
                    .fileName(fileName)
                    .totalRegistros(pessoas.size())
                    .sucesso(true)
                    .tempoProcessamento(duration)
                    .build();

            log.info("Processamento concluído: {} registros em {}ms",
                    pessoas.size(), duration);

            return result;

        } catch (Exception e) {
            log.error("Erro ao processar arquivo: {}", fileName, e);

            long duration = System.currentTimeMillis() - startTime;

            return ProcessamentoResult.builder()
                    .fileName(fileName)
                    .sucesso(false)
                    .erro(e.getMessage())
                    .tempoProcessamento(duration)
                    .build();
        }
    }

    /**
     * Parseia o conteúdo CSV e converte em lista de PessoaDTO
     */
    private List<PessoaDTO> parsearCSV(String csvContent, String sourceFile) {
        List<PessoaDTO> pessoas = new ArrayList<>();

        try (StringReader reader = new StringReader(csvContent);
             CSVParser csvParser = new CSVParser(reader,
                     CSVFormat.DEFAULT
                             .builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .setTrim(true)
                             .build())) {

            for (CSVRecord record : csvParser) {
                try {
                    PessoaDTO pessoa = PessoaDTO.builder()
                            .nome(record.get("nome"))
                            .idade(Integer.parseInt(record.get("idade")))
                            .cidade(record.get("cidade"))
                            .messageId(UUID.randomUUID().toString())
                            .processedAt(LocalDateTime.now())
                            .sourceFile(sourceFile)
                            .build();

                    pessoas.add(pessoa);

                } catch (Exception e) {
                    log.warn("Erro ao parsear linha {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Erro ao parsear CSV", e);
            throw new RuntimeException("Erro ao processar CSV", e);
        }

        return pessoas;
    }

    public void startCadastro() {
        log.info("Iniciando processamento do Cadastro");
        s3Service.listFiles().stream()
                .filter(name -> name.endsWith("pessoas.csv"))
                .forEach(this::processarArquivo);
    }

    /**
     * Classe para retornar estatísticas do processamento
     */
    @Data
    @Builder
    public static class ProcessamentoResult {
        private String fileName;
        private Integer totalRegistros;
        private Boolean sucesso;
        private String erro;
        private Long tempoProcessamento;
    }
}