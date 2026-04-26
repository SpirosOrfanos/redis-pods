package pam.services.player.api;


import aw.libs.redislib.configuration.InvalidationMessage;
import aw.libs.redislib.configuration.MultiTierCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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
            var response = multiTierCache.get(type);
            if (response == null) {
                return multiTierCache.get(type, () -> {
                    var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "ownerCache", type);

                    eventPublisher.publishEvent(event);
                    System.out.println("from cache");
                    return "ownerRepository.findByType" + (type);
                });
            }
            System.out.println("direct return");
            return response.toString();

        }
        var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "ownerCache", type);
        eventPublisher.publishEvent(event);
        System.out.println("publish from db");
        return "ownerRepository.findByType" + (type);
    }


    @EventListener
    public void handleOwnerUpdate(InvalidationMessage event) {
        System.out.println("handleOwnerUpdate "+event);
        Cache cache = cacheManager.getCache("ownerCache");
        cache.evict(event.getType());
    }

}
