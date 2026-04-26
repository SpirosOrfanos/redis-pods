package pam.services.player.api;


import aw.libs.redislib.configuration.InvalidationMessage;
import aw.libs.redislib.configuration.MultiTierCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MultiTierCacheManager.class);


    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping(path = "/owner")
    public String owner(@RequestParam(name = "type", defaultValue = "none") String type) {

        Cache cache = cacheManager.getCache("ownerCache");

        if (cache instanceof MultiTierCacheManager.MultiTierCache) {
            MultiTierCacheManager.MultiTierCache multiTierCache = (MultiTierCacheManager.MultiTierCache) cache.getNativeCache();
            var response = multiTierCache.get(type);
            if (response == null) {
                return multiTierCache.get(type, () -> {
                    logger.debug("from cache");
                    return "ownerRepository.findByType" + (type);
                });
            }
            logger.debug("direct return");
            return response.toString();

        }
        var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "ownerCache", type);
        eventPublisher.publishEvent(event);
        logger.debug("publish from db");
        return "ownerRepository.findByType" + (type);
    }

    @GetMapping(path = "/player")
    public String player(@RequestParam(name = "type", defaultValue = "none") String type) {

        Cache cache = cacheManager.getCache("playerCache");

        if (cache instanceof MultiTierCacheManager.MultiTierCache) {
            MultiTierCacheManager.MultiTierCache multiTierCache = (MultiTierCacheManager.MultiTierCache) cache.getNativeCache();
            var response = multiTierCache.get(type);
            if (response == null) {
                return multiTierCache.get(type, () -> {
                    logger.debug("player from cache");
                    return "playerRepository.findByType" + (type);
                });
            }
            logger.debug("direct return player");
            return response.toString();

        }
        var event = new InvalidationMessage(InvalidationMessage.Type.EVICT, "playerCache", type);
        eventPublisher.publishEvent(event);
        logger.debug("player publish from db");
        return "playerRepository.findByType" + (type);
    }


    @EventListener
    public void handleOwnerUpdate(InvalidationMessage event) {
        System.out.println("handleOwnerUpdate "+event.getCacheName());
        cacheManager.getCache(event.getCacheName()).evict(event.getType());
        cacheManager.getCache(event.getCacheName()).evict(event.getType());
    }

}
