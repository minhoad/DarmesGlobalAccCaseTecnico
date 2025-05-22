package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config.aspect;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(name = "cache.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class CacheMetricsAspect {

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;

    private final ConcurrentMap<String, Counter> hitCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> missCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCounters = new ConcurrentHashMap<>();

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object trackCacheMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        String[] cacheNames = cacheable.value();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        String cacheKey = generateCacheKey(methodName, joinPoint.getArgs());

        boolean cacheHit = false;
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.get(cacheKey) != null) {
                cacheHit = true;
                incrementHitCounter(cacheName);
                break;
            }
        }

        if (!cacheHit) {
            Arrays.stream(cacheNames).forEach(this::incrementMissCounter);
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            Arrays.stream(cacheNames).forEach(this::incrementErrorCounter);
            throw e;
        }
    }

    private String generateCacheKey(String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder(methodName);
        if (args != null && args.length > 0) {
            sb.append(":");
            for (Object arg : args) {
                sb.append(arg != null ? arg.toString() : "null").append(",");
            }
            sb.deleteCharAt(sb.length() - 1); // Remove last comma
        }
        return sb.toString();
    }

    private void incrementHitCounter(String cacheName) {
        hitCounters.computeIfAbsent(cacheName, name ->
                Counter.builder("cache.hits")
                        .tag("cache", name)
                        .description("Cache hit count")
                        .register(meterRegistry)
        ).increment();
    }

    private void incrementMissCounter(String cacheName) {
        missCounters.computeIfAbsent(cacheName, name ->
                Counter.builder("cache.misses")
                        .tag("cache", name)
                        .description("Cache miss count")
                        .register(meterRegistry)
        ).increment();
    }

    private void incrementErrorCounter(String cacheName) {
        errorCounters.computeIfAbsent(cacheName, name ->
                Counter.builder("cache.errors")
                        .tag("cache", name)
                        .description("Cache error count")
                        .register(meterRegistry)
        ).increment();
    }
}
