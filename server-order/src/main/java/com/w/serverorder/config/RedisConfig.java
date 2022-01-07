package com.w.serverorder.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    String host;

    @Value("${spring.redis.port}")
    String port;

    @Value("${spring.redis.database}")
    int database;

//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://" + host + ":" + port)
//                .setDatabase(database);
//        return Redisson.create(config);
//    }
//
//    // 这里使用的是docker创建的redis服务
//    @Bean
//    public RedissonClient redissonRed1() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://192.168.177.99:6479")
//                .setDatabase(database);
//        return Redisson.create(config);
//    }
//
//    @Bean
//    public RedissonClient redissonRed2() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://192.168.177.99:6579")
//                .setDatabase(database);
//        return Redisson.create(config);
//    }
//
//    @Bean
//    public RedissonClient redissonRed3() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://192.168.177.99:6679")
//                .setDatabase(database);
//        return Redisson.create(config);
//    }
}
