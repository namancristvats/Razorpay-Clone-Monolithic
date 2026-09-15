package com.ncv.razorpay.merchant.mapper;


import com.ncv.razorpay.merchant.dto.request.MerchantRequestSignup;
import com.ncv.razorpay.merchant.dto.response.MerchantResponse;
import com.ncv.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toEntityFromSignUpRequest(MerchantRequestSignup request);

    MerchantResponse toResponse(Merchant merchant);
}
