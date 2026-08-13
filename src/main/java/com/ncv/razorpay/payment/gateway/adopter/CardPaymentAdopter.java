package com.ncv.razorpay.payment.gateway.adopter;

import com.ncv.razorpay.payment.gateway.PaymentAdopter;
import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CardPaymentAdopter implements PaymentAdopter {
    @Override
    public PaymentResult initiate(PaymentRequest request) {
        return null;
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
