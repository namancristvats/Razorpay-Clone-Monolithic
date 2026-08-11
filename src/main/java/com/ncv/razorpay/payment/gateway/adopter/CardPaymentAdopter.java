package com.ncv.razorpay.payment.gateway.adopter;

import com.ncv.razorpay.payment.gateway.PaymentAdopter;
import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;

public class CardPaymentAdopter implements PaymentAdopter {
    @Override
    public PaymentResult initiate(PaymentRequest request) {
        return null;
    }
}
