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

    private String url;

    private Integer port;

    public List<AWCache> getCache() {
        return cache;
    }

    public void setCache(List<AWCache> cache) {
        this.cache = cache;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
