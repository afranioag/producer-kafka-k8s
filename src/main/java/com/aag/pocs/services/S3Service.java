package com.aag.pocs.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Lê o conteúdo de um arquivo do S3 e retorna como String
     */
    public String readFileAsString(String fileName) {
        log.info("Lendo arquivo do S3: bucket={}, key={}", bucketName, fileName);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

            String content = new BufferedReader(
                    new InputStreamReader(response, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            log.info("Arquivo lido com sucesso: {} bytes", content.length());
            return content;

        } catch (S3Exception e) {
            log.error("Erro ao ler arquivo do S3: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao ler arquivo do S3: " + fileName, e);
        }
    }

    /**
     * Lista todos os arquivos no bucket
     */
    public List<String> listFiles() {
        log.info("Listando arquivos no bucket: {}", bucketName);

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<String> fileNames = listResponse.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            log.info("Encontrados {} arquivos no bucket", fileNames.size());
            return fileNames;

        } catch (S3Exception e) {
            log.error("Erro ao listar arquivos do S3: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar arquivos do S3", e);
        }
    }

    /**
     * Verifica se um arquivo existe no bucket
     */
    public boolean fileExists(String fileName) {
        try {
            s3Client.headObject(builder -> builder
                    .bucket(bucketName)
                    .key(fileName));
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}