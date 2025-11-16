package com.aag.pocs.schedulle;

import com.aag.pocs.services.CadastroService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProducerJob {

    private final  CadastroService cadastroService;

    @Scheduled(fixedRate = 100)
    public void start() {
        cadastroService.startCadastro();
    }
}
