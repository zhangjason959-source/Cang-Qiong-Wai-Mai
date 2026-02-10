package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

        @Bean
        public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
            log.info("开始创建redis模板对象...");
            RedisTemplate redisTemplate = new RedisTemplate();
            //设置redis的连接工厂对象
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            //设置redis key的序列化器
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate;
        }

     /**
     * 创建redis连接工厂对象
     * @return
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("117.72.32.59");
        configuration.setPort(6379);
        configuration.setPassword("guqier6379");
        return new LettuceConnectionFactory(configuration);
    }
}