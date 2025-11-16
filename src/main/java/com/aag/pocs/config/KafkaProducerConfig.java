package com.aag.pocs.config;

import com.aag.pocs.dto.PessoaDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, PessoaDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Configuração básica
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configurações de confiabilidade
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Aguarda confirmação de todas as réplicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Tenta reenviar até 3 vezes
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Evita duplicatas

        // Configurações de performance
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Tamanho do batch em bytes
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Aguarda 10ms antes de enviar batch
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compressão dos dados

        // Configuração do JSON Serializer
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PessoaDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}