package com.ncv.razorpay.payment.api;

import com.ncv.razorpay.payment.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {
    List<Payment> findUnsettledCapturedPayments(UUID merchantId);
}
