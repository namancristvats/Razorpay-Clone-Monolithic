package com.ncv.razorpay.payment.paymentProcessor.strategy;

import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;

public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";

        String bankCode=request.methodDetails() !=null?
                request.methodDetails().get("vpa").toString(): null;
        if(VPA_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef= "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
