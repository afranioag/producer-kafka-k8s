package com.aag.pocs.producer;

import com.aag.pocs.dto.PessoaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, PessoaDTO> kafkaTemplate;

    @Value("${kafka.topic.csv-data}")
    private String topic;

    /**
     * Envia uma única mensagem para o Kafka
     * Retorna CompletableFuture para processamento assíncrono
     */
    public CompletableFuture<SendResult<String, PessoaDTO>> sendMessage(PessoaDTO pessoa) {
        log.debug("Enviando mensagem para Kafka: {}", pessoa);

        // Usa o nome como chave para garantir ordem de mensagens da mesma pessoa
        String key = pessoa.getNome();

        CompletableFuture<SendResult<String, PessoaDTO>> future =
                kafkaTemplate.send(topic, key, pessoa);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Mensagem enviada com sucesso: {} - Offset: {}",
                        pessoa.getNome(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Erro ao enviar mensagem: {}", pessoa.getNome(), ex);
            }
        });

        return future;
    }

    /**
     * Envia uma lista de mensagens em lote
     * Usa CompletableFuture.allOf para aguardar todas as mensagens
     */
    public CompletableFuture<Void> sendBatch(List<PessoaDTO> pessoas) {
        log.info("Iniciando envio de {} mensagens para o Kafka", pessoas.size());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Cria array de futures
        CompletableFuture<SendResult<String, PessoaDTO>>[] futures = pessoas.stream()
                .map(pessoa -> {
                    CompletableFuture<SendResult<String, PessoaDTO>> future = sendMessage(pessoa);

                    future.whenComplete((result, ex) -> {
                        if (ex == null) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    });

                    return future;
                })
                .toArray(CompletableFuture[]::new);

        // Aguarda todas as mensagens serem enviadas
        return CompletableFuture.allOf(futures)
                .whenComplete((result, ex) -> {
                    log.info("Envio finalizado - Sucesso: {} | Erros: {}",
                            successCount.get(), errorCount.get());
                });
    }

    /**
     * Envia mensagens de forma síncrona (aguarda confirmação)
     * Útil para casos onde você precisa garantir o envio antes de continuar
     */
    public void sendMessageSync(PessoaDTO pessoa) {
        try {
            log.debug("Enviando mensagem síncrona: {}", pessoa);

            SendResult<String, PessoaDTO> result =
                    kafkaTemplate.send(topic, pessoa.getNome(), pessoa).get();

            log.debug("Mensagem enviada com sucesso - Offset: {}",
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem síncrona: {}", pessoa.getNome(), e);
            throw new RuntimeException("Falha ao enviar mensagem para Kafka", e);
        }
    }
}