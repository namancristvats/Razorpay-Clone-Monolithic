package com.ncv.razorpay.operations.repository;

import com.ncv.razorpay.operations.entity.SettlementPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, UUID> {
}
