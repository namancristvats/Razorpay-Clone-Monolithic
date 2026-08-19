package com.ncv.razorpay.payment.simulator;

import com.ncv.razorpay.common.enums.ChaosMode;
import com.ncv.razorpay.common.enums.PaymentStatus;
import com.ncv.razorpay.common.util.RandomizerUtil;
import com.ncv.razorpay.payment.entity.Payment;
import com.ncv.razorpay.payment.repository.PaymentRepository;
import com.ncv.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks(){

        LocalDateTime golbalWindow=LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates=paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING,golbalWindow);

        log.info("Simulating Payments for {} payments",candidates.size());

        for(Payment payment:candidates){
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
    SimulatorConfig.MethodSimulatorConfig methodConfig=simulatorConfig.forConfig(payment.getMethod());

    LocalDateTime dueAt=dueAt(payment,methodConfig);
    if(LocalDateTime.now().isBefore(dueAt)){
        return;
    }
    ChaosMode chaosMode=simulatorConfig.getChaosMode();
    switch(chaosMode){
        case SUCCESS->resolve(payment,true);
        case FAILURE->resolve(payment,false);
        case TIMEOUT->{
            log.debug("BankCallback simulator: Payment Timed out");
        }
        case NORMAL,SLOW->resolve(payment,shouldApprove(payment,methodConfig));
    }
    }
    private void resolve(Payment payment,boolean approved){
        if(approved){
            String bankRef="SIM_BANK_REF_"+ RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(),true,bankRef,null,null);
        }else{
            paymentService.resolveAuthorization(payment.getId(),false,null,"SIMULATED_BANK_ERROR_CODE","Simulated Bank Declined");
        }
    }
    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig){
            int bucket=Math.abs(payment.getId().hashCode()) % 100;
            return bucket<methodConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment,SimulatorConfig.MethodSimulatorConfig methodConfig){

        int range=methodConfig.getMaxDelaySeconds()- methodConfig.getMinDelaySeconds();
        int delaySeconds= methodConfig.getMinDelaySeconds()+Math.abs(payment.getId().hashCode())%(range+1);

        if(simulatorConfig.getChaosMode()== ChaosMode.SLOW){
            delaySeconds*=2;
        }
        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }

}
