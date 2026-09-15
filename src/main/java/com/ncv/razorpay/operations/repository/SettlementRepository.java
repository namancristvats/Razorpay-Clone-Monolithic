package com.ncv.razorpay.operations.repository;

import com.ncv.razorpay.common.enums.SettlementStatus;
import com.ncv.razorpay.operations.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByStatus(SettlementStatus settlementStatus);
}
