package com.ncv.razorpay.payment.controller;

import com.ncv.razorpay.payment.dto.request.PaymentInitrequest;
import com.ncv.razorpay.payment.dto.response.PaymentResponse;
import com.ncv.razorpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    UUID merchantId=UUID.fromString("de66e443-da9e-446a-bbd2-a6c1b34114e4");

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody @Valid PaymentInitrequest request){
        PaymentResponse paymentResponse=paymentService.initiate(request,merchantId);
        return ResponseEntity.ok(paymentResponse);
    }
    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable UUID paymentId){
        PaymentResponse paymentResponse=paymentService.capture(paymentId,merchantId);
        return ResponseEntity.ok(paymentResponse);
    }
}
