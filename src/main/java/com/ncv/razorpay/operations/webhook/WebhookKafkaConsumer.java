package com.ncv.razorpay.operations.webhook;

import com.ncv.razorpay.common.dto.WebhookTarget;
import com.ncv.razorpay.common.enums.WebhookEventStatus;
import com.ncv.razorpay.common.util.SignerUtil;
import com.ncv.razorpay.merchant.api.MerchantLookupService;
import com.ncv.razorpay.operations.entity.WebhookEvent;
import com.ncv.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookKafkaConsumer {
    private final WebhookEventRepository webhookEventRepository;
    private final MerchantLookupService merchantLookupService;
    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;
    private final WebhookRetryQueue webhookRetryQueue;
    private final WebhookDlqRecorder dlqRecorder;

    @KafkaListener(topics = {
            "${app.kafka.topics.payment:payments.events}",
            "${app.kafka.topics.order:orders.events}",
            "${app.kafka.topics.refund:refunds.events}",
            "${app.kafka.topics.settlement:settlements.events}",
    })
    public void onWebhookEvent(ConsumerRecord<String, Map<String,Object>> record, Acknowledgment ack){
        try{
            Map<String,Object> envelope=record.value();
            Map<String,Object> data= (Map<String, Object>) envelope.get("data");
            String eventType= (String) envelope.get("eventType");

            Object merchantIdRaw=data.get("merchantId");
            if(merchantIdRaw==null){
                log.warn("No Merchant Id was found, skipping event: {}",eventType);
                ack.acknowledge();
                return;
            }
            UUID merchantId=UUID.fromString(merchantIdRaw.toString());
            //Get Configuration for the merchant to send the webhook event to the target URL
            //If no configuration is found, skip the event
            //If configuration is found, send the event to the target URL
            List<WebhookTarget> targets=merchantLookupService.getActiveConfigsForEvent(merchantId,eventType);
            if(targets.isEmpty()){
                log.warn("No Webhook target was found, skipping event: {}",eventType);
                ack.acknowledge();
                return;
            }
            Map<String,Object> signatureData=Map.of("eventType",eventType,
                    "payload",data);

            String signatureJson=objectMapper.writeValueAsString(signatureData);

            for(WebhookTarget target:targets){
                String signature= signerUtil.sign(signatureJson, target.webhookSecret());
                WebhookEvent webhookEvent=WebhookEvent
                        .builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();
                webhookEvent=webhookEventRepository.save(webhookEvent);
                webhookRetryQueue.enqueue(webhookEvent.getId(),webhookEvent.getNextRetryAt());
                log.info("Webhook event created for id: {}",webhookEvent.getId());
            }
            ack.acknowledge();
        }catch (DataAccessException | CannotCreateTransactionException dbDown) {
            log.error("Webhook consumer failed due to DB down, Could not process the record, offset: {}", record.offset(), dbDown);
        }
        catch (Exception logicError) {
            log.error("Webhook consumer failed due to logical error, Could not process the record, offset: {}", record.offset(), logicError);
            dlqRecorder.recordConsumerFailed(record, logicError.getMessage());
            ack.acknowledge();
        }
    }
}
