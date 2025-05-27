package in.market.goblin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class RedisPublisher {
    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    public void publish(ByteBuffer buffer) {
        try {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            redisTemplate.convertAndSend("tbt-stream", data);
            System.out.println("Published TBT data: " + data.length + " bytes");
        } catch (Exception e) {
            System.err.println("Error publishing TBT data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}