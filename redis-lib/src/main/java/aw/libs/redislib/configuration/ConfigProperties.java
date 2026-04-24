package aw.libs.redislib.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.caching")
@ConfigurationPropertiesScan
@Component
public class ConfigProperties {
    private List<AWCache> cache;

    public List<AWCache> getCache() {
        return cache;
    }

    public void setCache(List<AWCache> cache) {
        this.cache = cache;
    }
}
