package pam.services.player.api;


import aw.libs.redislib.configuration.MultiTierCacheManager;
import aw.libs.redislib.configuration.MultiTierCacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(MultiTierCacheManager.class);




    @GetMapping(path = "/owner")
    @MultiTierCacheable(cacheNames = "ownerCache", key = "#type", ttlSeconds = 300)
    public String owner(@RequestParam(name = "type", defaultValue = "none") String type) {
        return "ownerRepository.findByType" + (type);
    }

    @GetMapping(path = "/player")
    @MultiTierCacheable(cacheNames = "playerCache", key = "#type", ttlSeconds = 300)
    public String player(@RequestParam(name = "type", defaultValue = "none") String type) {
        return "playerRepository.findByType" + (type);
    }



}
