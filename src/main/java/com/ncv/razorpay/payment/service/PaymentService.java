package com.ncv.razorpay.payment.service;

import com.ncv.razorpay.payment.dto.request.PaymentInitrequest;
import com.ncv.razorpay.payment.dto.response.PaymentResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(@Valid PaymentInitrequest request, UUID merchantId);

    PaymentResponse capture(UUID paymentId, UUID merchantId);

    void resolveAuthorization(UUID id, boolean approve, String bankRef, String errorCode, String errorDescription);
}
