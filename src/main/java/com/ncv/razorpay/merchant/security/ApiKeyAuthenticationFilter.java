package com.ncv.razorpay.merchant.security;

import com.ncv.razorpay.common.exception.RateLimitException;
import com.ncv.razorpay.common.ratelimit.RateLimitResult;
import com.ncv.razorpay.common.ratelimit.RateLimiter;
import com.ncv.razorpay.merchant.cache.ApiKeyCache;
import com.ncv.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.ncv.razorpay.merchant.entity.ApiKey;
import com.ncv.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.persistence.Column;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX="Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYPT=new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;

    private final RateLimiter rateLimiter;
    @Value("${app.rate-limit.use-case.api-key.request-per-minute:60}")
    private Integer requestsPerMinute;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming request: {}", request.getRequestURI());
        try{
            String authorizationHeader= request.getHeader("Authorization");
            if(authorizationHeader==null || !authorizationHeader.startsWith(BASIC_PREFIX)){
               filterChain.doFilter(request,response);
               return;
            }
            //        Authorization: Basic key_asdlfjaosduf:secret_asdflauouadf
            //        Authorization: Basic ASDFUAOSJDFLAKSJDFA89SDUFLIJalsdjflakjsdflk==
            String[] credential=decode(authorizationHeader);
            if(credential==null){
                throw new BadRequestException("Malformed API key Header");
            }
            String keyId=credential[0];
            String rawSecret=credential[1];
//            ApiKey apiKey=apiKeyRepository.findByKeyId(keyId).orElseThrow(()->new BadRequestException("Invalid or Missing ApiKey"));

            ApiKeyCacheEntry apiKeyCacheEntry=apiKeyCache.get(keyId).orElseGet(()->loadAndCache(keyId));

            if(apiKeyCacheEntry==null || !apiKeyCacheEntry.enabled() || !secretMatches(rawSecret,apiKeyCacheEntry)){
                throw new BadRequestException("Invalid or missing API key");
            }

            RateLimitResult rateLimitResult=rateLimiter.check("apikey:"+keyId,requestsPerMinute,60);
            if(!rateLimitResult.isAllowed()){
                log.warn("Too many requests keyId={}", keyId);
                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
            }
            response.setHeader("X-RateLimit-Limit",String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining",String.valueOf(rateLimitResult.remaining()));

            Authentication authentication=new UsernamePasswordAuthenticationToken(
                    keyId,null, List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            merchantContext.setKeyId(keyId);
            merchantContext.setMerchantId(apiKeyCacheEntry.merchantId());
            filterChain.doFilter(request,response);
        }catch(Exception e){
            log.info("Incoming api key request in catch block");
            handlerExceptionResolver.resolveException(request,response,null,e);
        }
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey=apiKeyRepository.findByKeyId(keyId).orElse(null);
        if(apiKey==null)return null;
        ApiKeyCacheEntry apiKeyCacheEntry=new ApiKeyCacheEntry(
                apiKey.getKeyId(),
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getGracePeriodExpiresAt(),
                apiKey.getMerchant().getId(),
                apiKey.getEnvironment(),
                apiKey.isEnabled()
        );
        apiKeyCache.put(keyId,apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }

    private String[] decode(String header){
        String encoded=header.substring(BASIC_PREFIX.length());
        String decode=new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon=decode.indexOf(":");
        if(colon<1)return null;
        return new String[]{decode.substring(0,colon),decode.substring(colon+1)};
    }
    private boolean secretMatches(String rawSecret,ApiKeyCacheEntry apiKey) {
        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())) {
            return true;
        }
        return apiKey.isInGracePeriod() &&
                apiKey.previousKeySecretHash() != null &&
                BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash());
    }
}
