package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            connection.ping();

            return Health.up()
                    .withDetail("type", "Redis")
                    .withDetail("version", connection.info().getProperty("redis_version"))
                    .build();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("type", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error checking Redis health", e);
            return Health.down()
                    .withDetail("type", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
