package com.ncv.razorpay.payment.mapper;


import com.ncv.razorpay.payment.dto.response.OrderResponse;
import com.ncv.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord orderRecord);
}
