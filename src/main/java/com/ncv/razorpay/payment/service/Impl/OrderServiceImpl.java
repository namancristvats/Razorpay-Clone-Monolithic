package com.ncv.razorpay.payment.service.Impl;

import com.ncv.razorpay.common.enums.OrderStatus;
import com.ncv.razorpay.common.exception.DuplicateResourceException;
import com.ncv.razorpay.merchant.repository.MerchantRepository;
import com.ncv.razorpay.payment.dto.request.CreateOrderRequest;
import com.ncv.razorpay.payment.dto.response.OrderResponse;
import com.ncv.razorpay.payment.entity.OrderRecord;
import com.ncv.razorpay.payment.repository.OrderRepository;
import com.ncv.razorpay.payment.repository.PaymentRepository;
import com.ncv.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + request.receipt());
        }
        OrderRecord record=OrderRecord.builder()
                .amount(request.amount())
                .notes(request.notes())
                .receipt(request.receipt())
                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt()!=null? request.expiresAt():
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        record=orderRepository.save(record);
        // TODO:        publish kafka event about order creation
        return new OrderResponse(record.getId(),record.getMerchantId(),record.getReceipt()
        ,record.getAmount(),record.getOrderStatus(),record.getAttempts(),record.getNotes(),record.getExpiresAt(),
                LocalDateTime.now());

    }
}
