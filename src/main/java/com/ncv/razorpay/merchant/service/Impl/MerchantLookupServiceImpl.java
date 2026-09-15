package com.ncv.razorpay.merchant.service.Impl;

import com.ncv.razorpay.common.dto.SettlementBankDetails;
import com.ncv.razorpay.common.dto.WebhookTarget;
import com.ncv.razorpay.common.enums.MerchantStatus;
import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.merchant.api.MerchantLookupService;
import com.ncv.razorpay.merchant.entity.Merchant;
import com.ncv.razorpay.merchant.entity.MerchantWebhookConfig;
import com.ncv.razorpay.merchant.repository.MerchantRepository;
import com.ncv.razorpay.merchant.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantLookupServiceImpl implements MerchantLookupService {

    private final WebhookConfigRepository merchantWebhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;
    private final MerchantRepository merchantRepository;
    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return merchantWebhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId)
                .stream().filter(config->config.isSubscribedTo(eventType))
                .map(config->{
                    byte[] cypherByte= Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedSecretBytes=bytesEncryptor.decrypt(cypherByte);
                    return new WebhookTarget(config.getId(), config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                }).toList();
    }
    @Override
    public List<UUID> listActiveMerchantIds() {
       return merchantRepository.findByStatus(MerchantStatus.ACTIVE)
                .stream().map(merchant->merchant.getId()).toList();
    }

    @Override
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        Merchant merchant=merchantRepository.findById(merchantId).orElseThrow(()->
                new ResourceNotFoundException("Merchant",merchantId));
        return new SettlementBankDetails(merchant.getSettlementBankAccount(), merchant.getSettlementBankIfsc(), merchant.getSettlementBankAccountHolderName());
    }


}
