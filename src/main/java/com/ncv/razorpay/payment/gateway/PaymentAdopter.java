package com.ncv.razorpay.payment.gateway;

import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;

public interface PaymentAdopter {
    PaymentResult initiate(PaymentRequest request);
}
