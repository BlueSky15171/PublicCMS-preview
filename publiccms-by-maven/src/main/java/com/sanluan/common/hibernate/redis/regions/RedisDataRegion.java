package com.sanluan.common.hibernate.redis.regions;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.Region;

import com.sanluan.common.base.Base;
import com.sanluan.common.hibernate.redis.ConfigurableRedisRegionFactory;
import com.sanluan.common.hibernate.redis.RedisClient;
import com.sanluan.common.hibernate.redis.strategy.RedisAccessStrategyFactory;
import com.sanluan.common.hibernate.redis.timestamper.CacheTimestamper;

/**
 * @author zhangxdr
 *
 */
public abstract class RedisDataRegion extends Base implements Region {

    private static final String EXPIRE_IN_SECONDS = "redis.expiryInSeconds";
    protected final RedisAccessStrategyFactory accessStrategyFactory;

    private final String regionName;

    protected final RedisClient redisClient;

    private final CacheTimestamper cacheTimestamper;

    private final int expireInSeconds;

    /**
     * @param accessStrategyFactory
     * @param redis
     * @param configurableRedisRegionFactory
     * @param regionName
     * @param props
     */
    public RedisDataRegion(RedisAccessStrategyFactory accessStrategyFactory, RedisClient redisClient,
            ConfigurableRedisRegionFactory configurableRedisRegionFactory, String regionName, Properties props) {
        this.accessStrategyFactory = accessStrategyFactory;
        this.redisClient = redisClient;
        this.regionName = regionName;
        this.expireInSeconds = Integer
                .valueOf(props.getProperty(EXPIRE_IN_SECONDS + "." + regionName, getDefaultExpireInSeconds(props)));
        this.cacheTimestamper = configurableRedisRegionFactory.createCacheTimestamper(redisClient, regionName);
        log.debug("redisClient region=" + regionName);
    }

    /**
     * Region regionName
     *
     * @return region regionName
     */
    public String getName() {
        return regionName;
    }

    /**
     * delete region
     */
    @Override
    public void destroy() throws CacheException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.cache.spi.Region#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object key) {
        try {
            log.debug("contains key=" + key);
            return redisClient.exists(regionName, key);
        } catch (Exception ignored) {
            log.warn("Fail to exists key. key=" + key, ignored);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.cache.spi.Region#getSizeInMemory()
     */
    @Override
    public long getSizeInMemory() {
        try {
            long sizeInMemory = redisClient.dbSize();
            log.trace("size in memory. region=" + regionName + ", size=" + sizeInMemory);
            return sizeInMemory;
        } catch (Exception ignored) {
            log.warn("Fail to get size in memory.", ignored);
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.cache.spi.Region#getElementCountInMemory()
     */
    @Override
    public long getElementCountInMemory() {
        return redisClient.getDataSize(regionName);
    }

    @Override
    public long getElementCountOnDisk() {
        return -1;
    }

    @Override
    public Map<?, ?> toMap() {
        return redisClient.getAll(regionName);
    }

    @Override
    public long nextTimestamp() {
        return cacheTimestamper.next();
    }

    public int getExpireInSeconds() {
        return expireInSeconds;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    private static String getDefaultExpireInSeconds(final Properties props) {
        return props.getProperty(EXPIRE_IN_SECONDS, String.valueOf(RedisClient.DEFAULT_EXPIRY_IN_SECONDS));
    }
}
