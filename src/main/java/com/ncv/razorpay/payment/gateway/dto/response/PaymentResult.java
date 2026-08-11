package com.ncv.razorpay.payment.gateway.dto.response;

public sealed interface PaymentResult permits PaymentResult.Pending, PaymentResult.Failure, PaymentResult.Success {
    record Pending(String registrationRef) implements PaymentResult{}
    record Failure(String errorCode,String errorDescription) implements PaymentResult{}
    record Success(String bankReference) implements PaymentResult{}
}
