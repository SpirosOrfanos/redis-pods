package aw.libs.redislib.configuration;

public class AWCache {
    private String name;
    private Integer ttl;
    private Integer maxEntries;
    private Integer localCacheExpiration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public Integer getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(Integer maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AWCache{");
        sb.append("name='").append(name).append('\'');
        sb.append(", ttl=").append(ttl);

        sb.append('}');
        return sb.toString();
    }
}
