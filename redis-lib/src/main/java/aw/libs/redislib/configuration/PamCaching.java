package aw.libs.redislib.configuration;

import org.springframework.stereotype.Component;

@Component
public class PamCaching {
    private final ConfigProperties configProperties;

    public PamCaching(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public void cache() {
        for (AWCache cache : configProperties.getCache()) {
            System.out.println(cache);
        }
    }
}
