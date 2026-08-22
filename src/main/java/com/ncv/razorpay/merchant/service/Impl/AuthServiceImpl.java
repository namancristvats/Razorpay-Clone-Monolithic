package com.ncv.razorpay.merchant.service.Impl;

import com.ncv.razorpay.common.enums.MerchantStatus;
import com.ncv.razorpay.common.enums.UserRole;
import com.ncv.razorpay.common.exception.DuplicateResourceException;
import com.ncv.razorpay.common.exception.ResourceNotFoundException;
import com.ncv.razorpay.merchant.dto.request.LoginRequest;
import com.ncv.razorpay.merchant.dto.request.MerchantRequestSignup;
import com.ncv.razorpay.merchant.dto.response.LoginResponse;
import com.ncv.razorpay.merchant.dto.response.MerchantResponse;
import com.ncv.razorpay.merchant.entity.AppUser;
import com.ncv.razorpay.merchant.entity.Merchant;
import com.ncv.razorpay.merchant.repository.AppUserRepository;
import com.ncv.razorpay.merchant.repository.MerchantRepository;
import com.ncv.razorpay.merchant.security.JwtUtil;
import com.ncv.razorpay.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantRequestSignup request) {
            if(merchantRepository.existsByEmail(request.email())){
                throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL","Merchant with email already exists: "+request.email());
        }
        Merchant merchant=Merchant.builder()
                .businessName(request.businessName())
                .email(request.email())
                .name(request.name())
                .businessType(request.businessType())
                .status(MerchantStatus.PENDING_KYC)
                .build();
            merchant=merchantRepository.save(merchant);
            AppUser appUser= AppUser.builder()
                    .email(request.email())
                    .merchant(merchant)
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .role(UserRole.OWNER)
                    .build();
            appUserRepository.save(appUser);

            return new MerchantResponse(merchant.getId(),merchant.getName(),merchant.getEmail()
            , merchant.getBusinessName(), merchant.getBusinessType(),merchant.getStatus());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),request.password()));

        AppUser appUser=appUserRepository.findByEmail(request.email()).orElseThrow(()->new ResourceNotFoundException("User",request.email()));
        String token=jwtUtil.generateAccessToken(request.email(),appUser.getMerchant().getId(),appUser.getRole().toString());
        return new LoginResponse(token);
    }
}
