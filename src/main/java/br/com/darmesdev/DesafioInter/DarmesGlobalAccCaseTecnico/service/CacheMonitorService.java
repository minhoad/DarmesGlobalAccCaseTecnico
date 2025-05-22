package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheMonitorService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    /**
     * Logs cache statistics every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void logCacheStatistics() {
        try {
            // Log basic cache information
            log.info("Cache manager: {}", cacheManager.getClass().getSimpleName());
            log.info("Available caches: {}", String.join(", ", cacheManager.getCacheNames()));

            // Try to get Redis keys
            try {
                Set<String> keys = redisTemplate.keys("*");
                if (keys == null || keys.isEmpty()) {
                    log.info("No cache entries found");
                    return;
                }

                log.info("Total cache entries: {}", keys.size());

                // Group by cache name
                Map<String, Long> entriesByCache = keys.stream()
                        .collect(Collectors.groupingBy(
                                key -> {
                                    String[] parts = key.split("::");
                                    return parts.length > 0 ? parts[0] : "unknown";
                                },
                                Collectors.counting()
                        ));

                entriesByCache.forEach((cache, count) ->
                        log.info("Cache '{}': {} entries", cache, count));
            } catch (RedisConnectionFailureException e) {
                log.warn("Could not connect to Redis for statistics: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error while logging cache statistics", e);
        }
    }

    /**
     * Get cache statistics as a map
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Get all cache names
            cacheManager.getCacheNames().forEach(name -> {
                stats.put(name + ".available", "true");
            });

            // Try to count entries per cache
            try {
                Set<String> keys = redisTemplate.keys("*");
                if (keys != null && !keys.isEmpty()) {
                    stats.put("totalEntries", keys.size());

                    // Group by cache name
                    Map<String, Long> entriesByCache = keys.stream()
                            .collect(Collectors.groupingBy(
                                    key -> {
                                        String[] parts = key.split("::");
                                        return parts.length > 0 ? parts[0] : "unknown";
                                    },
                                    Collectors.counting()
                            ));

                    entriesByCache.forEach((cache, count) ->
                            stats.put(cache + ".entries", count));
                }
            } catch (RedisConnectionFailureException e) {
                log.warn("Could not connect to Redis for statistics: {}", e.getMessage());
                stats.put("redisAvailable", false);
                stats.put("cacheManager", cacheManager.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Check if a key exists in the cache
     */
    public boolean isCached(String cacheName, String key) {
        try {
            String fullKey = cacheName + "::" + key;
            Boolean exists = redisTemplate.hasKey(fullKey);
            return Boolean.TRUE.equals(exists);
        } catch (RedisConnectionFailureException e) {
            log.warn("Could not connect to Redis to check key: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear a specific cache
     */
    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            try {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            } catch (Exception e) {
                log.error("Error clearing cache '{}': {}", cacheName, e.getMessage());
            }
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                try {
                    cache.clear();
                    log.info("Cleared cache: {}", name);
                } catch (Exception e) {
                    log.error("Error clearing cache '{}': {}", name, e.getMessage());
                }
            }
        });
        log.info("All caches cleared");
    }
}
