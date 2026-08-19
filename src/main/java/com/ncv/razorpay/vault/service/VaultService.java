package com.ncv.razorpay.vault.service;

import com.ncv.razorpay.common.entity.Money;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;
import com.ncv.razorpay.vault.dto.request.TokenizeRequest;
import com.ncv.razorpay.vault.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(@Valid TokenizeRequest request, UUID merchantId);
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String,Object> methodDetails);
}
