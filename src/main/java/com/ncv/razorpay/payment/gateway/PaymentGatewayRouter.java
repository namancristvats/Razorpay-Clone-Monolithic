package com.ncv.razorpay.payment.gateway;

import com.ncv.razorpay.common.enums.PaymentMethod;
import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {
        private final Map<PaymentMethod,PaymentAdopter> paymentAdopters;

        public PaymentResult initiate(PaymentRequest request){
            PaymentAdopter adopter=paymentAdopters.get(request.method());
            if(adopter==null){
                throw new IllegalArgumentException("No payment adopter registered for method: "+request.method());
            }
            return adopter.initiate(request);
        }
}
