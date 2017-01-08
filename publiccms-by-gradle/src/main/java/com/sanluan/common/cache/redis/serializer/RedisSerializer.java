package com.sanluan.common.cache.redis.serializer;

public interface RedisSerializer<T> {
    public byte[] EMPTY_BYTES = new byte[0];

    public byte[] serialize(final T graph);

    public T deserialize(final byte[] bytes);
}