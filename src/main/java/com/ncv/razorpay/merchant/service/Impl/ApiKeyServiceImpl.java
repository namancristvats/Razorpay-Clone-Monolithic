package com.ncv.razorpay.merchant.service.Impl;

import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.ncv.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.ncv.razorpay.merchant.dto.response.ApiKeyResponse;
import com.ncv.razorpay.merchant.entity.ApiKey;
import com.ncv.razorpay.merchant.entity.Merchant;
import com.ncv.razorpay.merchant.repository.ApiKeyRepository;
import com.ncv.razorpay.merchant.repository.MerchantRepository;
import com.ncv.razorpay.merchant.service.ApiKeyService;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant=merchantRepository.findById(merchantId).orElseThrow(()->
                new ResourceNotFoundException("merchant",merchantId));
        String keyID="rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret=RandomizerUtil.randomBase64(40);
        ApiKey apiKey= ApiKey.builder()
                .merchant(merchant)
                .keyId(keyID)
                .keySecretHash(rawSecret)//TODO : Encode with BCrypt Password
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(apiKey.getId(), apiKey.getKeyId(),apiKey.getKeySecretHash(),apiKey.getEnvironment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchant_Id(merchantId).stream()
                .map(apiKey -> new ApiKeyResponse(apiKey.getId(), apiKey.getKeyId(),apiKey.getEnvironment()
                ,apiKey.isEnabled(),apiKey.getLastUsedAt(),null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey key=apiKeyRepository.findById(keyId)
                .filter(k->k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("Api Key",keyId));
        key.setEnabled(false);
    }

    @Override
    @Transactional
    public @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey key=apiKeyRepository.findById(keyId).filter(k->k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("Api Key",keyId));
        if(!key.isEnabled()) throw new RuntimeException("Cannot rotate a disabled key");
        String newRawSecret=RandomizerUtil.randomBase64(40);
        key.setPreviousKeySecretHash(key.getKeySecretHash());
        key.setKeySecretHash(newRawSecret);
        key.setRotatedAt(LocalDateTime.now());
        key.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        key=apiKeyRepository.save(key);
        return new ApiKeyCreateResponse(key.getId(),key.getKeyId(),key.getKeySecretHash(),key.getEnvironment());
    }
}
