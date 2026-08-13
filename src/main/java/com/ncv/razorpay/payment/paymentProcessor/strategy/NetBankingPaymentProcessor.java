package com.ncv.razorpay.payment.paymentProcessor.strategy;

import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.payment.paymentProcessor.PaymentProcessor;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorRequest;
import com.ncv.razorpay.payment.paymentProcessor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode=request.methodDetails() !=null?
                request.methodDetails().get("BANK").toString(): null;
        if(BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("BANK_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef= "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        //        String redirectRef = "http://REDIRECT_BANK.com/"+processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
