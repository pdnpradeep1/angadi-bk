package com.ecom.pradeep.angadi_bk.config;

//@Configuration
//@EnableCaching
public class RedisConfig {

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory(); // Uses Lettuce for Redis connection
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(60)) // Cache expiry time
//                .disableCachingNullValues()
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//
//        return RedisCacheManager.builder(redisConnectionFaectory)
//                .cacheDefaults(cacheConfig)
//                .build();
//    }
//
//    @Bean
//    public SimpleKeyGenerator keyGenerator() {
//        return new SimpleKeyGenerator();
//    }
}
