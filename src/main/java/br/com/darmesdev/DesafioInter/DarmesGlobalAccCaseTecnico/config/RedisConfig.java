package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    private final ObjectMapper objectMapper;
    private final CacheProperties cacheProperties;

    @Value("${cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${cache.fallback-to-local:true}")
    private boolean fallbackToLocal;

    public RedisConfig(ObjectMapper objectMapper, CacheProperties cacheProperties) {
        this.objectMapper = objectMapper;
        this.cacheProperties = cacheProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host:localhost}") String host,
            @Value("${spring.redis.port:6379}") int port,
            @Value("${spring.redis.password:}") String password,
            @Value("${spring.redis.timeout:2000}") long timeout) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            config.setPassword(RedisPassword.of(password));
        }

        // Configure Lettuce client with timeout
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);

            GenericJackson2JsonRedisSerializer serializer =
                    new GenericJackson2JsonRedisSerializer(objectMapper);

            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(serializer);

            template.afterPropertiesSet();
            return template;
        } catch (Exception e) {
            log.error("Failed to create RedisTemplate: {}", e.getMessage());
            if (fallbackToLocal) {
                log.warn("Using a simple RedisTemplate with no connection. Operations will fail at runtime.");
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.afterPropertiesSet();
                return template;
            }
            throw e;
        }
    }

    @Primary
    @Bean
    @Override
    public CacheManager cacheManager() {
        if (!cacheEnabled) {
            log.info("Caching is disabled. Using NoOpCacheManager");
            return new org.springframework.cache.support.NoOpCacheManager();
        }

        try {
            return redisCacheManager();
        } catch (Exception e) {
            log.error("Failed to create Redis cache manager: {}", e.getMessage());
            if (fallbackToLocal) {
                log.warn("Falling back to local cache manager");
                return new ConcurrentMapCacheManager();
            }
            throw e;
        }
    }

    @Bean(name = "redisCacheManager")
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager redisCacheManager() {
        try {
            RedisConnectionFactory connectionFactory = redisConnectionFactory(
                    cacheProperties.getRedisHost(),
                    cacheProperties.getRedisPort(),
                    cacheProperties.getRedisPassword(),
                    cacheProperties.getRedisTimeout()
            );

            // Test connection
            try {
                connectionFactory.getConnection().ping();
                log.info("Successfully connected to Redis");
            } catch (Exception e) {
                log.error("Failed to connect to Redis: {}", e.getMessage());
                if (fallbackToLocal) {
                    log.warn("Falling back to local cache manager");
                    return new ConcurrentMapCacheManager();
                }
                throw e;
            }

            Duration defaultTtl = cacheProperties.getDefaultTtl();
            if (defaultTtl == null) {
                defaultTtl = Duration.ofMinutes(30); // Default 30 minutes if not specified
            }

            log.info("Configuring Redis cache with default TTL: {}", defaultTtl);

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(defaultTtl)
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                            new GenericJackson2JsonRedisSerializer(objectMapper)
                    ));

            // Apply prefix if configured
            if (cacheProperties.getKeyPrefix() != null && !cacheProperties.getKeyPrefix().isEmpty()) {
                defaultConfig = defaultConfig.prefixCacheNameWith(cacheProperties.getKeyPrefix());
                log.info("Using cache key prefix: {}", cacheProperties.getKeyPrefix());
            }

            // Create cache-specific configurations
            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

            // Add configurations from properties
            if (cacheProperties.getTtls() != null) {
                RedisCacheConfiguration finalDefaultConfig = defaultConfig;
                cacheProperties.getTtls().forEach((cacheName, ttl) -> {
                    log.info("Configuring cache '{}' with TTL: {}", cacheName, ttl);
                    cacheConfigurations.put(cacheName, finalDefaultConfig.entryTtl(ttl));
                });
            }

            // Build the cache manager
            RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .transactionAware();

            return builder.build();
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to connect to Redis: {}", e.getMessage());
            if (fallbackToLocal) {
                log.warn("Falling back to local cache manager");
                return new ConcurrentMapCacheManager();
            }
            throw e;
        }
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }

    /**
     * Custom error handler that logs cache errors but doesn't fail the application
     */
    @Slf4j
    public static class CustomCacheErrorHandler implements CacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            log.error("Error getting from cache '{}' with key '{}': {}",
                    cache.getName(), key, exception.getMessage());
            log.debug("Cache error details:", exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
            log.error("Error putting to cache '{}' with key '{}': {}",
                    cache.getName(), key, exception.getMessage());
            log.debug("Cache error details:", exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
            log.error("Error evicting from cache '{}' with key '{}': {}",
                    cache.getName(), key, exception.getMessage());
            log.debug("Cache error details:", exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
            log.error("Error clearing cache '{}': {}",
                    cache.getName(), exception.getMessage());
            log.debug("Cache error details:", exception);
        }
    }
}
