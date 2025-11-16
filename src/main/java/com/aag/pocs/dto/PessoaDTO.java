package com.aag.pocs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PessoaDTO implements Serializable {

    private String nome;
    private Integer idade;
    private String cidade;

    // Metadados úteis para tracking
    private String messageId;
    private LocalDateTime processedAt;
    private String sourceFile;

    @Override
    public String toString() {
        return String.format("Pessoa[nome=%s, idade=%d, cidade=%s]", nome, idade, cidade);
    }
}