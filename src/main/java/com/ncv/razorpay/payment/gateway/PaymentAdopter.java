package com.ncv.razorpay.payment.gateway;

import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;

import java.util.UUID;

public interface PaymentAdopter {
    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
