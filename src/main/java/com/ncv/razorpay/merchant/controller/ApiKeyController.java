package com.ncv.razorpay.merchant.controller;

import com.ncv.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.ncv.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.ncv.razorpay.merchant.dto.response.ApiKeyResponse;
import com.ncv.razorpay.merchant.entity.Merchant;
import com.ncv.razorpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@RequestBody CreateApiKeyRequest request, @PathVariable UUID merchantId){
        return new ResponseEntity<>(apiKeyService.create(merchantId,request),HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listByMerchantId(@PathVariable UUID merchantId){
        return new ResponseEntity<>(apiKeyService.listByMerchant(merchantId),HttpStatus.OK);
    }
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID merchantId, @PathVariable UUID keyId){
        apiKeyService.revoke(merchantId,keyId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotateKey(@PathVariable UUID merchantId,@PathVariable UUID keyId){
        return new ResponseEntity<>(apiKeyService.rotate(merchantId,keyId),HttpStatus.OK);
    }
}
