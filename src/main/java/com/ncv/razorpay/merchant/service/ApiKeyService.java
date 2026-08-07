package com.ncv.razorpay.merchant.service;

import com.ncv.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.ncv.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.ncv.razorpay.merchant.dto.response.ApiKeyResponse;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);
    public List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
