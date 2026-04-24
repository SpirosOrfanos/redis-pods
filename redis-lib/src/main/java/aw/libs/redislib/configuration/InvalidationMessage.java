package aw.libs.redislib.configuration;

import java.io.Serializable;

public class InvalidationMessage implements Serializable {
    public enum Type { EVICT, CLEAR }
    private Type type;
    private String cacheName;
    private String key;

    public InvalidationMessage() {
    }

    public InvalidationMessage(Type type, String cacheName, String key) {
        this.type = type;
        this.cacheName = cacheName;
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}