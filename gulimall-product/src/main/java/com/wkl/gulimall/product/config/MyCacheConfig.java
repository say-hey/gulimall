package com.wkl.gulimall.product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableConfigurationProperties(CacheProperties.class)
@Configuration
public class MyCacheConfig {

    //方法一
    @Autowired
    CacheProperties cacheProperties;


    /**
     * redis配置
     *
     * 由于获取的是默认配置，所以配置文件中的东西没有用到，现在需要生效，关联配置文件CacheProperties.class，然后设置
     * 方法一、
     *      1、原来和配置文件绑定的配置类是这样的
     *      @ConfigurationProperties(prefix="spring.cache")
     *      public class CacheProperties
     *      2、让他生效
     *      @EnableConfigurationProperties(CacheProperties.class)
     * 方法二、
     *      1、所有方法的参数都是从容器中获取，直接在方法参数中获取就行了
     *
     *
     * @return
     */
    @Bean                               //方法二
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        //先取出默认设置，然后修改某些设置，然后再返回，也可以自定义新建，这样可以直接设置ttl
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        //修改key的序列化，还使用默认的
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        //修改value的序列化，使用转json的
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        //由于使用的是默认的RedisCacheConfiguration配置修改的，它使用的默认的-1，
        //所以是因为自己没改，现在需要配置生效，需要关联配置文件，取出内容并设置，
        //配置文件映射是CacheProperties.class

        //将配置文件中的所有配置都生效
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //设置配置文件中的各项配置，如过期时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
