package aw.libs.redislib.configuration;

import java.lang.annotation.*;
import org.springframework.core.annotation.AliasFor;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiTierCacheable {

    @AliasFor("cacheNames")
    String[] value() default {};

    @AliasFor("value")
    String[] cacheNames() default {};

    String key() default "";

    String condition() default "";

    String unless() default "";

    long ttlSeconds() default 300; // 5 minutes default

    boolean sync() default true;
}