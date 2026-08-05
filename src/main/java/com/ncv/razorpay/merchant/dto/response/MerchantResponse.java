package com.ncv.razorpay.merchant.dto.response;

import com.ncv.razorpay.common.enums.BusinessType;
import com.ncv.razorpay.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
