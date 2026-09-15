package com.ncv.razorpay.payment.outbox;

import com.ncv.razorpay.common.config.KafkaProperties;
import com.ncv.razorpay.common.enums.OutboxStatus;
import com.ncv.razorpay.payment.entity.OutboxEvent;
import com.ncv.razorpay.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Scheduled(fixedDelay = 5000)
    public void poll(){
        List<OutboxEvent> pendingEvents=outboxEventRepository.
                findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for(OutboxEvent event:pendingEvents){
            try{
                String topic=kafkaProperties.topicFor(event.getAggregateType());
                String key=extractMerchant(event.getPayload());

                Map<String,Object> envelope=Map.of(
                        "eventType",event.getEventType(),
                        "aggregateType",event.getAggregateType().name(),
                        "aggregateId",event.getAggregateId().toString(),
                        "data",event.getPayload()
                );
                kafkaTemplate.send(topic,key,envelope)
                        .get(5, TimeUnit.SECONDS);
            }
            catch (Exception e) {
                log.error("Outbox event failed, eventId: {}, attempts: {}", event.getId(), event.getAttempts());

            }
        }
    }

    private String extractMerchant(Map<String,Object> payload){
        Object value=payload.get("merchantId");
        return value !=null ? value.toString():"unknown";
    }


}
