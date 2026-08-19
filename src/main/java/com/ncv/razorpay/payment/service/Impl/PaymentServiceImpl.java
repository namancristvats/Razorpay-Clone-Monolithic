package com.ncv.razorpay.payment.service.Impl;

import com.ncv.razorpay.common.enums.OrderStatus;
import com.ncv.razorpay.common.enums.PaymentEvent;
import com.ncv.razorpay.common.enums.PaymentStatus;
import com.ncv.razorpay.common.exception.BusinessRuleViolationException;
import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.payment.dto.request.PaymentInitrequest;
import com.ncv.razorpay.payment.dto.response.PaymentResponse;
import com.ncv.razorpay.payment.entity.OrderRecord;
import com.ncv.razorpay.payment.entity.Payment;
import com.ncv.razorpay.payment.gateway.PaymentGatewayRouter;
import com.ncv.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.ncv.razorpay.payment.gateway.dto.response.PaymentResult;
import com.ncv.razorpay.payment.mapper.PaymentMapper;
import com.ncv.razorpay.payment.repository.OrderRepository;
import com.ncv.razorpay.payment.repository.PaymentRepository;
import com.ncv.razorpay.payment.service.PaymentService;
import com.ncv.razorpay.payment.statemachine.PaymentTransitionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter router;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionLogService paymentTransitionLogService;
    @Override
    @Transactional()
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
        PaymentRequest paymentRequest=
                new PaymentRequest(payment.getId(),request.orderId(),merchantId,order.getAmount(),request.method(),request.methodDetails());

        paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult paymentResult=router.initiate(paymentRequest);

        switch (paymentResult){
            case PaymentResult.Failure failure->{
//                payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success->{
                log.info("Invalid State");
                return null;
            }
            case PaymentResult.Pending pending->{
                payment.setProcessorReference(pending.registrationRef());
            }
        }
        payment=paymentRepository.save(payment);
        orderRepository.save(order);
        // TODO: send an outbox (kafka event)
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID paymentId, UUID merchantId) {
        Payment payment=paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));
//        payment.setStatus(PaymentStatus.CAPTURING);  //TODO statemachine implementation
        paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult result=router.capture(payment.getMethod(),paymentId);
        if(result instanceof PaymentResult.Success success){
            log.info("Payment Captured, paymentId: {}",paymentId);
//            payment.setStatus(PaymentStatus.CAPTURED);
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());

        }
        else if(result instanceof PaymentResult.Failure failure){
//            payment.setStatus(PaymentStatus.AUTHORIZED);
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.info("Payment Captured Failed, paymentId: {}",paymentId);
        }
        payment=paymentRepository.save(payment);
        return paymentMapper.toResponse(payment);

    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {
        Payment payment=paymentRepository.findById(paymentId)
                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));

        if(payment.getStatus()!=PaymentStatus.AUTHORIZING){
            log.warn("Payment is not in Authorizing state,paymentId:{} and status {}",paymentId,payment.getStatus());
            return;
        }
        OrderRecord order=payment.getOrder();
        if(approve) {
            paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            //Auto Capture
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = router.capture(payment.getMethod(), paymentId);
            if (captureResult instanceof PaymentResult.Success success) {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                order.setOrderStatus(OrderStatus.PAID);

            } else if (captureResult instanceof PaymentResult.Failure failure) {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }
            else{
                paymentTransitionLogService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
            }
            paymentRepository.save(payment);
            orderRepository.save(order);
            //Todo publish kafka events
        }
    }

