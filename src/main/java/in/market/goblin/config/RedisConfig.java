package in.market.goblin.config;

import in.market.goblin.service.RedisFileWriter;
import in.market.goblin.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        // config.setPassword(RedisPassword.of("your-password")); // Uncomment if needed
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, byte[]> redisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new RawByteArraySerializer());
        template.setEnableDefaultSerializer(false);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public MessageListenerAdapter messageListener(RedisSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new RawByteArraySerializer());
        return adapter;
    }

    @Bean
    public MessageListenerAdapter fileWriterListener(RedisFileWriter subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new RawByteArraySerializer());
        return adapter;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            JedisConnectionFactory connectionFactory,
            @Qualifier("messageListener") MessageListenerAdapter listenerAdapter,
            @Qualifier("fileWriterListener") MessageListenerAdapter fileWriterListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic("tbt-stream"));
        container.addMessageListener(fileWriterListener, new ChannelTopic("tbt-stream"));
        return container;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("tbt-stream");
    }
}