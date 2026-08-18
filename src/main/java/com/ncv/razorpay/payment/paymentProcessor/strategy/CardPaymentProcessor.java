package com.ncv.razorpay.payment.paymentProcessor.strategy;

import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {
    public static final String PAN_CARD_DECLINED="4000000000000002";
    public static final String PAN_CARD_EXPIRED="4000000000000069";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        if (PAN_CARD_DECLINED.equals(request.pan())) {
            log.warn("Card declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by bank");
        }

        if (PAN_CARD_EXPIRED.equals(request.pan())) {
            log.warn("Pan card has expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired");
        }
        String processorRef="CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
