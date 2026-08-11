package com.ncv.razorpay.payment.config;

import com.ncv.razorpay.common.enums.PaymentMethod;
import com.ncv.razorpay.payment.gateway.PaymentAdopter;
import com.ncv.razorpay.payment.gateway.adopter.CardPaymentAdopter;
import com.ncv.razorpay.payment.gateway.adopter.NetBankingAdopter;
import com.ncv.razorpay.payment.gateway.adopter.UpiPaymentAdopter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdopterConfig {
    private final CardPaymentAdopter cardPaymentAdopter;
    private final UpiPaymentAdopter upiPaymentAdopter;
    private final NetBankingAdopter netBankingAdopter;

    @Bean
    public Map<PaymentMethod, PaymentAdopter> paymentAdopterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdopter,
                PaymentMethod.UPI, upiPaymentAdopter,
                PaymentMethod.NETBANKING, netBankingAdopter
        );
    }

}
