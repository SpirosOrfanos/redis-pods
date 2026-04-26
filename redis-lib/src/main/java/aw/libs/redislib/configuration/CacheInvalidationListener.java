package aw.libs.redislib.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class CacheInvalidationListener implements MessageListener {

    private final Map<String, MultiTierCacheManager.MultiTierCache> caches = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private MultiTierCacheManager cacheManager;

    @EventListener(ApplicationReadyEvent.class)
    public void registerCaches() {
        this.cacheManager = applicationContext.getBean(MultiTierCacheManager.class);

        // Optional: Pre-populate the caches map for faster access
        /*if (this.cacheManager != null) {
            for (String cacheName : this.cacheManager.getCacheNames()) {
                Cache cache = this.cacheManager.getCache(cacheName);
                if (cache != null && cache.getNativeCache() instanceof MultiTierCacheManager.MultiTierCache) {
                    caches.put(cacheName, (MultiTierCacheManager.MultiTierCache) cache.getNativeCache());
                }
            }
        }*/
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("Message received " + message);
        try {
            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
            InvalidationMessage invalidation = (InvalidationMessage)
                    serializer.deserialize(message.getBody());

            if (invalidation != null) {
                handleInvalidation(invalidation);
            }
        } catch (Exception e) {
            System.err.println("Error processing cache invalidation message: " + e.getMessage());
        }
    }

    private void handleInvalidation(InvalidationMessage invalidation) {
        if (cacheManager == null) {
            System.err.println("CacheManager not initialized yet");
            return;
        }
        Cache cache = cacheManager.getCache(invalidation.getCacheName());

        if (cache != null && cache.getNativeCache() instanceof MultiTierCacheManager.MultiTierCache) {
            MultiTierCacheManager.MultiTierCache multiTierCache =
                    (MultiTierCacheManager.MultiTierCache) cache.getNativeCache();

            if (invalidation.getType() == InvalidationMessage.Type.EVICT) {
                multiTierCache.invalidateLocalOnly(invalidation.getKey());
            } else if (invalidation.getType() == InvalidationMessage.Type.CLEAR) {
                multiTierCache.clearLocalOnly();
            }
        }
    }
}