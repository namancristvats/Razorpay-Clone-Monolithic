package com.ncv.razorpay.merchant.service;

import com.ncv.razorpay.merchant.dto.request.UpdateConfigRequest;
import com.ncv.razorpay.merchant.dto.response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {
    WebhookConfigResponse create(UUID merchantId, UpdateConfigRequest request);
    List<WebhookConfigResponse> list(UUID merchantId);
    WebhookConfigResponse getById(UUID merchantId,UUID configId);
    WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateConfigRequest request);
    void delete(UUID merchantId, UUID configId);
}
