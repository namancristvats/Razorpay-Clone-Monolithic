package com.ncv.razorpay.vault.service.Impl;

import com.ncv.razorpay.common.entity.Money;
import com.ncv.razorpay.common.enums.CardBrand;
import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessorRouter;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;
import com.ncv.razorpay.vault.config.VaultEncryptionConfig;
import com.ncv.razorpay.vault.dto.request.TokenizeRequest;
import com.ncv.razorpay.vault.dto.response.TokenizeResponse;
import com.ncv.razorpay.vault.entity.CardToken;
import com.ncv.razorpay.vault.entity.VaultCard;
import com.ncv.razorpay.vault.repositories.CardTokenRepository;
import com.ncv.razorpay.vault.repositories.VaultCardRepository;
import com.ncv.razorpay.vault.service.VaultService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {
    private final VaultEncryptionConfig vaultEncryptionConfig;
    private final BytesEncryptor dekEncrypter;
    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final PaymentProcessorRouter paymentProcessorRouter;
    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String lastFour=request.pan().substring(request.pan().length()-4);
        String bin=request.pan().substring(0,6);
        CardBrand cardBrand=detectBrand(request.pan());
        byte[] dek= KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan=vaultEncryptionConfig.panEncrypter(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek=dekEncrypter.encrypt(dek);

        VaultCard vaultCard=VaultCard.builder()
                .lastFour(lastFour)
                .expiryMonth(request.expiryMonth().toString())
                .expiryYear(request.expiryYear().toString())
                .encryptedPan(encryptedPan)
                .brand(cardBrand)
                .bin(bin)
                .encryptedDek(encryptedDek)
                .cardHolderName(request.cardHolderName())
                .build();
        vaultCard=vaultCardRepository.save(vaultCard);
        String token="tok_"+ RandomizerUtil.randomBase64(32);
        CardToken cardToken= CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .customer(request.customerId())
                .merchant(merchantId)
                .build();
        cardToken=cardTokenRepository.save(cardToken);
        return new TokenizeResponse(token, lastFour, cardBrand, request.expiryMonth(), request.expiryYear());
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken=cardTokenRepository.findByTokenAndRevokedAtIsNull(token).orElseThrow(()->new ResourceNotFoundException("Card Token",token));
        VaultCard vaultCard=cardToken.getVaultCard();

        byte [] panBytes=null;
        try{
            byte[] dek= dekEncrypter.decrypt(vaultCard.getEncryptedDek());
            panBytes=vaultEncryptionConfig.panEncrypter(dek).decrypt(vaultCard.getEncryptedPan());
            String pan=new String(panBytes, StandardCharsets.UTF_8);

            String expiry= vaultCard.getExpiryMonth()+"/"+vaultCard.getExpiryYear();
            // Here you would call the payment processor's API to charge the card using the decrypted PAN
            PaymentProcessorRequest paymentProcessorRequest=PaymentProcessorRequest.card(
                    paymentId,pan,expiry,amount,methodDetails
            );
            PaymentProcessorResponse response=paymentProcessorRouter.charge(paymentProcessorRequest);
            log.info("Vault charge registered, token={}****", token.substring(0, 4));
        return response;
        }catch (Exception e) {
            log.warn("Vault charge failed, token={}****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if (panBytes != null) Arrays.fill(panBytes, (byte) 0);
        }
    }


    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4")) return CardBrand.VISA;
        if (pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if (pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
