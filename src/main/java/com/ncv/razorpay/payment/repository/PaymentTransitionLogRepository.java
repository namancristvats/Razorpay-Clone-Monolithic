package com.ncv.razorpay.payment.repository;

import com.ncv.razorpay.payment.entity.PaymentTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransitionLogRepository extends JpaRepository<PaymentTransitionLog, Long> {
}
