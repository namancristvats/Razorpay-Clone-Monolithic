package com.ncv.razorpay.vault.controller;

import com.ncv.razorpay.vault.dto.response.TokenizeResponse;
import com.ncv.razorpay.vault.dto.request.TokenizeRequest;
import com.ncv.razorpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {
    private final VaultService vaultService;
    UUID merchantId=UUID.fromString("de66e443-da9e-446a-bbd2-a6c1b34114e4");
    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest request){
        TokenizeResponse response=vaultService.tokenize(request,merchantId);
        return ResponseEntity.status(201).body(response);
    }
}
