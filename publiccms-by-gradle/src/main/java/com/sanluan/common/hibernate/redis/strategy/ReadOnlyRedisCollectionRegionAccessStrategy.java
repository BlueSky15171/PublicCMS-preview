package com.sanluan.common.hibernate.redis.strategy;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

import com.sanluan.common.hibernate.redis.regions.RedisCollectionRegion;

public class ReadOnlyRedisCollectionRegionAccessStrategy extends AbstractRedisAccessStrategy<RedisCollectionRegion>
        implements CollectionRegionAccessStrategy {

    public ReadOnlyRedisCollectionRegionAccessStrategy(RedisCollectionRegion region, SessionFactoryOptions options) {
        super(region, options);
    }

    @Override
    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory,
            String tenantIdentifier) {
        return DefaultCacheKeysFactory.createCollectionKey(id, persister, factory, tenantIdentifier);
    }

    @Override
    public Object getCacheKeyId(Object cacheKey) {
        return DefaultCacheKeysFactory.getCollectionId(cacheKey);
    }

    @Override
    public CollectionRegion getRegion() {
        return region;
    }

    @Override
    public Object get(SessionImplementor session, Object key, long txTimestamp) {
        return region.get(key);
    }

    @Override
    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version,
            boolean minimalPutOverride) {
        if (minimalPutOverride && region.contains(key)) {
            return false;
        }

        region.put(key, value);
        return true;
    }

    @Override
    public SoftLock lockItem(SessionImplementor session, Object key, Object version) {
        return null;
    }

    @Override
    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) {
    }

}
