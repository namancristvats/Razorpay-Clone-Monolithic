package com.ncv.razorpay.merchant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisApiKeyCache implements ApiKeyCache{

    private final String PREFIX="apikey:";
    private static final Duration TTL= Duration.ofMinutes(5);
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try{
            String json=stringRedisTemplate.opsForValue().get(PREFIX+keyId);
            if(json==null){
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json,ApiKeyCacheEntry.class));
        }catch(Exception e){
            log.warn("ApiKey cache read failed, keyId: {}", keyId);
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry apiKeyCacheEntry) {
        try{
            stringRedisTemplate.opsForValue().set(PREFIX+keyId,
                    objectMapper.writeValueAsString(apiKeyCacheEntry),TTL);
        }
        catch(Exception e){
            log.warn("ApiKey cache put failed, keyId: {}", keyId);
        }
    }

    @Override
    public void evict(String keyId) {
        stringRedisTemplate.delete(PREFIX+keyId);
    }
}
