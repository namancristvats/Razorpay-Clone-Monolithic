package com.ncv.razorpay.payment.gateway.dto.request;

import com.ncv.razorpay.common.entity.Money;
import com.ncv.razorpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod method,
        Map<String,Object> methodDetails
) {
}
