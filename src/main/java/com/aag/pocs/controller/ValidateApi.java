package com.aag.pocs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class ValidateApi {

    @GetMapping(value = "/api")
    public String  api() {
        return "api";
    }
}
