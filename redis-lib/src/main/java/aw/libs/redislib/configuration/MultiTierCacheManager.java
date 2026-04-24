package aw.libs.redislib.configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.support.SimpleValueWrapper;
import java.util.Set;

@Component
public class MultiTierCacheManager implements CacheManager {

    private final ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final Caffeine<Object, Object> caffeineBuilder;

    public MultiTierCacheManager(RedisConnectionFactory connectionFactory) {
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(connectionFactory);
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        this.redisTemplate.afterPropertiesSet();
        this.caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)  // L1 expires quickly
                .recordStats();  // For monitoring
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, this::createMultiTierCache);
    }

    private Cache createMultiTierCache(String name) {
        return new MultiTierCache(
                name,
                redisTemplate,
                caffeineBuilder.buildAsync().synchronous());
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    public static class MultiTierCache implements Cache {
        private final String name;
        private final RedisTemplate<String, Object> redisTemplate;
        private final com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache;
        private final String REDIS_KEY_PREFIX = "aw_cache::";

        public MultiTierCache(String name, RedisTemplate<String, Object> redisTemplate,
                              com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache) {
            this.name = name;
            this.redisTemplate = redisTemplate;
            this.localCache = localCache;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return this;
        }

        @Override
        public ValueWrapper get(Object key) {
            System.out.println("get ValueWrapper " + key);
            Object value = getInternal(key);
            return value != null ? new SimpleValueWrapper(value) : null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            System.out.println("get <T> T get " + key);
            Object value = getInternal(key);
            return type.cast(value);
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            System.out.println("get <T> T get Callable " + key);
            String cacheKey = buildKey(key);
            Object value = localCache.getIfPresent(cacheKey);
            if (value != null) {
                return (T) value;
            }

            value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                localCache.put(cacheKey, value);
                return (T) value;
            }

            try {
                value = valueLoader.call();

                if (value != null) {
                    put(key, value);
                }

                return (T) value;
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }

        private Object getInternal(Object key) {
            String cacheKey = buildKey(key);
            Object value = localCache.getIfPresent(cacheKey);
            if (value != null) {
                return value;
            }
            value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                localCache.put(cacheKey, value);
                return value;
            }

            return null;
        }

        @Override
        public void put(Object key, Object value) {
            System.out.println("put "+key);
            String cacheKey = buildKey(key);
            localCache.put(cacheKey, value);
            redisTemplate.opsForValue().set(cacheKey, value, 10, TimeUnit.MINUTES);
            publishInvalidation(cacheKey);
        }

        @Override
        public void evict(Object key) {
            System.out.println("evict "+key);
            String cacheKey = buildKey(key);
            localCache.invalidate(cacheKey);
            redisTemplate.delete(cacheKey);
            publishInvalidation(cacheKey);
        }

        @Override
        public void clear() {
            System.out.println("clear ");
            localCache.invalidateAll();
            Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + name + "::*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            publishClear();
        }

        private String buildKey(Object key) {
            System.out.println("buildKey " + key);
            return REDIS_KEY_PREFIX + name + "::" + key.toString();
        }

        private void publishInvalidation(String cacheKey) {
            System.out.println("publishInvalidation " + cacheKey);
            InvalidationMessage message = new InvalidationMessage(
                    InvalidationMessage.Type.EVICT,
                    name,
                    cacheKey
            );
            redisTemplate.convertAndSend("cache-invalidation", message);
        }

        private void publishClear() {
            System.out.println("publishClear ");
            InvalidationMessage message = new InvalidationMessage(
                    InvalidationMessage.Type.CLEAR,
                    name,
                    null
            );
            redisTemplate.convertAndSend("cache-invalidation", message);
        }
        public void invalidateLocalOnly(String key) {
            System.out.println("invalidateLocalOnly "+key);
            localCache.invalidate(key);
        }


        public void clearLocalOnly() {
            System.out.println("clearLocalOnly ");
            localCache.invalidateAll();
        }
    }
}