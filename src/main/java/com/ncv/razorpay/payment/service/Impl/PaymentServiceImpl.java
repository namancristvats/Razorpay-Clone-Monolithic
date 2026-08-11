package com.ncv.razorpay.payment.service.Impl;

import com.ncv.razorpay.common.enums.OrderStatus;
import com.ncv.razorpay.common.enums.PaymentStatus;
import com.ncv.razorpay.common.exception.BusinessRuleViolationException;
import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.payment.dto.request.PaymentInitrequest;
import com.ncv.razorpay.payment.dto.response.PaymentResponse;
import com.ncv.razorpay.payment.entity.OrderRecord;
import com.ncv.razorpay.payment.entity.Payment;
import com.ncv.razorpay.payment.repository.OrderRepository;
import com.ncv.razorpay.payment.repository.PaymentRepository;
import com.ncv.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    @Override
    public PaymentResponse initiate(PaymentInitrequest request, UUID merchantId) {
        OrderRecord order=orderRepository.findByIdAndMerchantId(request.orderId(),merchantId)
                .orElseThrow(()->new ResourceNotFoundException("order",request.orderId()));

        if(order.getOrderStatus()!= OrderStatus.CREATED && order.getOrderStatus()!=OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE","Order cannot accept payment in status: "+order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment= Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();
        payment=paymentRepository.save(payment);

        //Payment Gateway Implementation will be here.......
        return null;
    }
}
