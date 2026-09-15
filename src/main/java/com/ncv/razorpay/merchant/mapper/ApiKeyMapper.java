package com.ncv.razorpay.merchant.mapper;


import com.ncv.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.ncv.razorpay.merchant.dto.response.ApiKeyResponse;
import com.ncv.razorpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    ApiKeyCreateResponse toCreateResponse(ApiKey apiKey);

    List<ApiKeyResponse> toResponseList(List<ApiKey> apiKeyList);
}
