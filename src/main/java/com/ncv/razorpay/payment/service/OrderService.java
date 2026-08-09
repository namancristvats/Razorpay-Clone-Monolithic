package com.ncv.razorpay.payment.service;

import com.ncv.razorpay.payment.dto.request.CreateOrderRequest;
import com.ncv.razorpay.payment.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest request);
}
