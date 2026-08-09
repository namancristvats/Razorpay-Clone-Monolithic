package com.ncv.razorpay.payment.repository;

import com.ncv.razorpay.payment.entity.OrderRecord;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {
    boolean existsByMerchantIdAndReceipt(UUID merchantId, @Size(max = 100) String receipt);

    Optional<OrderRecord> findByIdAndMerchantId(@NotNull(message = "Order Id is required") UUID uuid, UUID merchantId);
}
