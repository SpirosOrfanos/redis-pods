package aw.libs.redislib.configuration;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MultiTierCacheableAspect {

    private static final Logger logger = LoggerFactory.getLogger(MultiTierCacheableAspect.class);
    @Autowired
    private CacheManager cacheManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(multiTierCacheable)")
    public Object cache(ProceedingJoinPoint joinPoint, MultiTierCacheable multiTierCacheable) throws Throwable {
        String cacheName = multiTierCacheable.cacheNames()[0];
        String keyExpression = multiTierCacheable.key();

        String key = generateKey(joinPoint, keyExpression);
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return joinPoint.proceed();
        }
        MultiTierCacheManager.MultiTierCache multiTierCache = (MultiTierCacheManager.MultiTierCache) cache.getNativeCache();
        return multiTierCache.get(key, () -> {
            try {
                logger.debug("========Loading from actual method for key: {}", key);
                return joinPoint.proceed();
            } catch (Throwable e) {
                logger.error("", e);
                throw new RuntimeException(e);
            }
        });
    }

    /*@EventListener
    public void handleOwnerUpdate(InvalidationMessage event) {
        logger.debug("handle {} {} {}", event.getCacheName(), event.getType(), event.getKey());
        cacheManager.getCache(event.getCacheName()).evict(event.getType());
    }*/

    private String generateKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        if (keyExpression.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            StringBuilder key = new StringBuilder(signature.getMethod().getName());
            for (Object arg : joinPoint.getArgs()) {
                key.append(":").append(arg);
            }
            return key.toString();
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
