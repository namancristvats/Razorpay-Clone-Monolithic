package com.ncv.razorpay.payment.paymentProcessor.strategy;

import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        return null;
    }
}
