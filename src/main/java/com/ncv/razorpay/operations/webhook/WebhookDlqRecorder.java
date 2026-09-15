package com.ncv.razorpay.operations.webhook;

import com.ncv.razorpay.common.enums.WebhookEventStatus;
import com.ncv.razorpay.operations.entity.DlqEvent;
import com.ncv.razorpay.operations.entity.WebhookEvent;
import com.ncv.razorpay.operations.repository.DlqEventRepository;
import com.ncv.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebhookDlqRecorder {
    private  final DlqEventRepository dlqEventRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAfterAttemptsExhausted(WebhookEvent webhookEvent,String error){
        log.debug("Recording the Dlq event with WebhookEventId {}",webhookEvent.getId());
        webhookEvent.setStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(webhookEvent);

        DlqEvent dlqEvent=DlqEvent.builder()
                .payload(webhookEvent.getPayload())
                .merchantId(webhookEvent.getMerchantId())
                .finalError(error)
                .webhookEvent(webhookEvent)
                .build();
        dlqEventRepository.save(dlqEvent);
    }

    public void recordConsumerFailed(ConsumerRecord<String, Map<String, Object>> record, String error) {
        Map<String, Object> envelope = record.value();

        UUID merchantId = null;
        try {
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            Object merchantIdRaw = data != null ? data.get("merchantId") : null;
            if (merchantIdRaw != null) {
                merchantId = UUID.fromString(merchantIdRaw.toString());
            }
        } catch (Exception ignored) {

        }
        log.debug("Recording the Dlq event because consumer failed with merchantId {}", merchantId);
        DlqEvent dlqEvent = DlqEvent
                .builder()
                .webhookEvent(null)
                .merchantId(merchantId)
                .finalError(error)
                .payload(envelope != null ? envelope : Map.of())
                .build();
        dlqEventRepository.save(dlqEvent);
    }
}
