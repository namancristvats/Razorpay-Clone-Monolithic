package com.ncv.razorpay.payment.gateway.adopter;

import com.ncv.razorpay.common.enums.PaymentMethod;
import com.ncv.razorpay.payment.gateway.PaymentAdopter;
import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessorRouter;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdopter implements PaymentAdopter {
    private final PaymentProcessorRouter paymentProcessorRouter;
    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiate PAyemnt with UpiPaymentAdopter, paymentId: {}",request.paymentId());
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.Noncard(
                    request.paymentId(), PaymentMethod.UPI, request.amount(), request.methodDetails()
            );

            PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);
            return switch (response) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("Upi failed, paymentId: {}", request.paymentId());
            return new PaymentResult.Failure("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_REF");
    }
}
