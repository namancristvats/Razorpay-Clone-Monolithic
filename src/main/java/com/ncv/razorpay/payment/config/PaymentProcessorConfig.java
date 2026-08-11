package com.ncv.razorpay.payment.config;

import com.ncv.razorpay.common.enums.PaymentMethod;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.strategy.CardPaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.strategy.NetBankingPaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap() {
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.NETBANKING, new NetBankingPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor()
        );
    }
}
