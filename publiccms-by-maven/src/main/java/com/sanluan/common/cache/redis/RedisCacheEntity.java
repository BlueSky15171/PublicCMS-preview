package com.sanluan.common.cache.redis;

import static org.apache.commons.lang3.ArrayUtils.addAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sanluan.common.cache.CacheEntity;
import com.sanluan.common.cache.redis.serializer.BinarySerializer;
import com.sanluan.common.cache.redis.serializer.StringSerializer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCacheEntity<K, V> implements CacheEntity<K, V>, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int size = 100;
    private JedisPool jedisPool;
    private byte[] name;
    private byte[] count;
    private final static StringSerializer regionSerializer = new StringSerializer();
    private final BinarySerializer<K> keySerializer = new BinarySerializer<K>();
    private final BinarySerializer<V> valueSerializer = new BinarySerializer<V>();

    public RedisCacheEntity(String name, JedisPool jedisPool) {
        this.name = regionSerializer.serialize(name);
        this.count = regionSerializer.serialize(name + "_count");
        this.jedisPool = jedisPool;
    }

    public RedisCacheEntity(String name, JedisPool jedisPool, int size) {
        this(name, jedisPool);
        this.size = size;
    }

    @Override
    public List<V> put(K key, V value) {
        Jedis jedis = jedisPool.getResource();
        byte[] fullKey = getKey(key);
        jedis.set(fullKey, valueSerializer.serialize(value));
        jedis.zadd(name, System.currentTimeMillis(), fullKey);
        return clearCache(jedis);
    }

    @Override
    public synchronized void put(K key, V value, Integer expiry) {
        Jedis jedis = jedisPool.getResource();
        byte[] fullKey = getKey(key);
        if (null == expiry) {
            jedis.set(fullKey, valueSerializer.serialize(value));
        } else {
            jedis.setex(fullKey, expiry, valueSerializer.serialize(value));
        }
        jedis.zadd(name, System.currentTimeMillis(), fullKey);
        jedis.incr(count);
        jedis.close();
    }

    @Override
    public synchronized V get(K key) {
        Jedis jedis = jedisPool.getResource();
        byte[] fullKey = getKey(key);
        V value = valueSerializer.deserialize(jedis.get(fullKey));
        jedis.zadd(name, System.currentTimeMillis(), fullKey);
        jedis.close();
        return value;
    }

    @Override
    public synchronized Collection<V> clear() {
        Jedis jedis = jedisPool.getResource();
        Set<byte[]> keyList = jedis.zrange(name, 0, -1);
        Collection<V> valueList = new ArrayList<V>();
        for (byte[] key : keyList) {
            valueList.add(valueSerializer.deserialize(key));
            jedis.del(key);
        }
        jedis.del(name);
        jedis.close();
        return valueList;
    }

    @Override
    public synchronized V remove(K key) {
        Jedis jedis = jedisPool.getResource();
        byte[] keybyte = getKey(key);
        if (jedis.exists(keybyte)) {
            V value = valueSerializer.deserialize(jedis.get(keybyte));
            jedis.del(keybyte);
            jedis.zrem(name, keybyte);
            jedis.close();
            return value;
        }
        return null;
    }

    @Override
    public long getDataSize() {
        Jedis jedis = jedisPool.getResource();
        long size = jedis.zrevrange(name, 0, -1).size();
        jedis.close();
        return size;
    }

    private synchronized List<V> clearCache(Jedis jedis) {
        List<V> list = null;
        if (size < jedis.incr(count)) {
            int helf = size / 2;
            jedis.decrBy(count, helf);
            Set<byte[]> keys = jedis.zrange(name, 0, helf);
            list = new ArrayList<V>();
            for (byte[] key : keys) {
                list.add(valueSerializer.deserialize(jedis.get(key)));
                jedis.zrem(name, key);
                jedis.del(key);
            }
        }
        jedis.close();
        return list;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public Map<K, V> getAll() {
        Jedis jedis = jedisPool.getResource();
        Set<byte[]> keySet = jedis.zrange(name, 0, -1);
        Map<K, V> map = new HashMap<K, V>();
        for (byte[] key : keySet) {
            map.put(keySerializer.deserialize(key), valueSerializer.deserialize(jedis.get(key)));
        }
        jedis.close();
        return map;
    }

    @Override
    public boolean contains(K key) {
        Jedis jedis = jedisPool.getResource();
        boolean exits = jedis.exists(getKey(key));
        jedis.close();
        return exits;
    }

    private byte[] getKey(K key) {
        return addAll(name, keySerializer.serialize(key));
    }
}
