package pam.services.player.api;


import aw.libs.redislib.configuration.InvalidationMessage;
import aw.libs.redislib.configuration.MultiTierCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Config {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping
    public String cll(@RequestParam(name = "type", defaultValue = "none") String type) {

        Cache cache = cacheManager.getCache("ownerCache");

        if (cache instanceof MultiTierCacheManager.MultiTierCache) {
            MultiTierCacheManager.MultiTierCache multiTierCache = (MultiTierCacheManager.MultiTierCache) cache.getNativeCache();
            return multiTierCache.get(type, () -> {
                var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "ownerCache", type);

                eventPublisher.publishEvent(event);
                System.out.println("from cacvche");
                return "ownerRepository.findByType" + (type);
            });
        }
        var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "ownerCache", type);
        eventPublisher.publishEvent(event);
        System.out.println("publish");
        return "ownerRepository.findByType" + (type);
    }


    @EventListener
    public void handleOwnerUpdate(InvalidationMessage event) {
        // When data changes, evict from cache
        System.out.println("received "+event);
        Cache cache = cacheManager.getCache("ownerCache");
        cache.evict(event.getType());

        // The invalidation message will be published through the cache
        // All other pods will receive it and invalidate their L1 caches
    }

}
