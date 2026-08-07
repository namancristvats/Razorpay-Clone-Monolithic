package com.ncv.razorpay.merchant.dto.request;

import com.ncv.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
