package in.market.goblin.config;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RawByteArraySerializer implements RedisSerializer<byte[]> {
    @Override
    public byte[] serialize(byte[] value) throws SerializationException {
        return value; // Pass through as-is
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws SerializationException {
        return bytes; // Pass through as-is
    }
}