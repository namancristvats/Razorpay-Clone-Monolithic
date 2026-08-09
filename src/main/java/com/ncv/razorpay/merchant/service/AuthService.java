package com.ncv.razorpay.merchant.service;

import com.ncv.razorpay.merchant.dto.request.MerchantRequestSignup;
import com.ncv.razorpay.merchant.dto.response.MerchantResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public interface AuthService {
        public MerchantResponse signup(MerchantRequestSignup request);
}
