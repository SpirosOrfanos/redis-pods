package aw.libs.redislib.configuration;

public class AWCache {
    private String name;
    private Integer ttl;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AWCache{");
        sb.append("name='").append(name).append('\'');
        sb.append(", ttl=").append(ttl);
        sb.append('}');
        return sb.toString();
    }
}
