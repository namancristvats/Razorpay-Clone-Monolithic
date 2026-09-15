package com.ncv.razorpay.operations.webhook;

import com.ncv.razorpay.common.enums.WebhookEventStatus;
import com.ncv.razorpay.operations.entity.WebhookEvent;
import com.ncv.razorpay.operations.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryScheduler {
    private final WebhookRetryQueue webhookRetryQueue;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookDeliveryExecutor webhookDeliveryExecutor;
    @Value("${app.webhook.delivery.poll-batch-size:1000}")
    private int batchSize;

    private ExecutorService virtualThreadExecutor;

    @PostConstruct
    void init(){
        virtualThreadExecutor= Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    void shutDown(){
        virtualThreadExecutor.shutdown();
    }

    @Scheduled(fixedDelayString = "1000")
    public void pollAndDelivery(){
        Set<UUID> due=webhookRetryQueue.pollDue(batchSize);

        if(due.isEmpty()){
          return;
        }
        for(UUID webhookEventId:due){
            //Executor
            //We are making RestCLient request in deliver() method which make time to do it but we don not want to
            //block thread while performing rest client calls, so we gonna use virtual thread that will do work in background.
            virtualThreadExecutor.submit(()->{
                webhookDeliveryExecutor.deliver(webhookEventId);
            });

        }

    }

    @Scheduled(fixedRate = 10000)
    public void reconcileFromDatabase(){
        LocalDateTime now=LocalDateTime.now();
        List<WebhookEvent> due=webhookEventRepository.findByStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING,now);
        for(WebhookEvent event:due){
            webhookRetryQueue.enqueueIfAbsent(event.getId(),event.getNextRetryAt());
        }
    }

}
