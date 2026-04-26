package aw.libs.redislib.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
@EnableCaching
public class PamCachingAutoConfiguration {

    @Bean("PamCaching")
    @ConditionalOnMissingBean
    //@ConditionalOnProperty(value = "pam.caching", havingValue = "enable")
    public PamCaching init(ConfigProperties configProperties) {
        return new PamCaching(configProperties);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ConfigProperties configProperties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName(configProperties.getUrl());
        factory.setPort(configProperties.getPort());
        factory.setDatabase(0);
        factory.setShutdownTimeout(100);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            CacheInvalidationListener invalidationListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(invalidationListener, new PatternTopic("cache-invalidation"));
        return container;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ConfigProperties configProperties) {
        return new MultiTierCacheManager(connectionFactory, configProperties);
    }
}
