package com.sanluan.common.cache.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sanluan.common.cache.CacheEntity;

public class MemoryCacheEntity<K, V> implements CacheEntity<K, V>, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int size;
    private LinkedHashMap<K, V> cachedMap = new LinkedHashMap<K, V>(16, 0.75f, true);

    public MemoryCacheEntity(int size) {
        this.size = size;
    }

    @Override
    public List<V> put(K key, V value) {
        cachedMap.put(key, value);
        return clearCache();
    }

    @Override
    public synchronized void put(K key, V value, Integer expiry) {
        cachedMap.put(key, value);
    }

    @Override
    public synchronized V get(K key) {
        return cachedMap.get(key);
    }

    @Override
    public synchronized Collection<V> clear() {
        Collection<V> values = cachedMap.values();
        cachedMap.clear();
        return values;
    }

    @Override
    public synchronized V remove(K key) {
        return cachedMap.remove(key);
    }

    private synchronized List<V> clearCache() {
        List<V> list = null;
        if (size < cachedMap.size()) {
            Iterator<K> iterator = cachedMap.keySet().iterator();
            List<K> keyList = new ArrayList<K>();
            for (int i = 0; i < size / 2; i++) {
                keyList.add(iterator.next());
            }
            list = new ArrayList<V>();
            for (K key : keyList) {
                list.add(cachedMap.remove(key));
            }
        }
        return list;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public long getDataSize() {
        return cachedMap.size();
    }

    @Override
    public Map<K, V> getAll() {
        return cachedMap;
    }

    @Override
    public boolean contains(K key) {
        return cachedMap.containsKey(key);
    }
}
