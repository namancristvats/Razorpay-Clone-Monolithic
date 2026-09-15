package com.ncv.razorpay.merchant.service.Impl;

import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.merchant.dto.request.UpdateConfigRequest;
import com.ncv.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.ncv.razorpay.merchant.entity.Merchant;
import com.ncv.razorpay.merchant.entity.MerchantWebhookConfig;
import com.ncv.razorpay.merchant.mapper.WebhookConfigMapper;
import com.ncv.razorpay.merchant.repository.MerchantRepository;
import com.ncv.razorpay.merchant.repository.WebhookConfigRepository;
import com.ncv.razorpay.merchant.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncrypter;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponse create(UUID merchantId, UpdateConfigRequest request) {
        Merchant merchant=merchantRepository.findById(merchantId).orElseThrow(()->
                new ResourceNotFoundException("Merchant",merchantId));
        String rawSecret= RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes=rawSecret.getBytes(StandardCharsets.UTF_8);
        String encryptedSecret= Base64.getEncoder().
                encodeToString(bytesEncrypter.encrypt(rawSecretBytes));

        MerchantWebhookConfig merchantWebhookConfig=MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .enabled(true)
                .build();
        merchantWebhookConfig=webhookConfigRepository.save(merchantWebhookConfig);
        return webhookConfigMapper.toResponse(merchantWebhookConfig, rawSecret);
    }

    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateConfigRequest request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());
        log.info("Merchant webhook config updated id={} merchantId={}", configId, merchantId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        webhookConfigRepository.delete(config);
        log.info("Merchant webhook config deleted id={} merchantId={}", configId, merchantId);
    }
    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId) {
        return webhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", configId));
    }
}
