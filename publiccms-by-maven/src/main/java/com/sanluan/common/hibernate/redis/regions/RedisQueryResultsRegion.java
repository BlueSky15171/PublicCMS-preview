package com.sanluan.common.hibernate.redis.regions;

import java.util.Properties;

import org.hibernate.cache.spi.QueryResultsRegion;

import com.sanluan.common.hibernate.redis.ConfigurableRedisRegionFactory;
import com.sanluan.common.hibernate.redis.RedisClient;
import com.sanluan.common.hibernate.redis.strategy.RedisAccessStrategyFactory;

/**
 * @author zhangxdr
 *
 */
public class RedisQueryResultsRegion extends RedisGeneralDataRegion implements QueryResultsRegion {

    /**
     * @param accessStrategyFactory
     * @param redis
     * @param configurableRedisRegionFactory
     * @param regionName
     * @param props
     */
    public RedisQueryResultsRegion(RedisAccessStrategyFactory accessStrategyFactory, RedisClient redis,
            ConfigurableRedisRegionFactory configurableRedisRegionFactory, String regionName, Properties props) {
        super(accessStrategyFactory, redis, configurableRedisRegionFactory, regionName, props);
    }
}
