package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.log-cache-operations", havingValue = "true", matchIfMissing = true)
public class CacheLoggingAspect {

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object logCacheableOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        String[] cacheNames = cacheable.value();
        String cacheKey = cacheable.key();

        if (cacheKey.isEmpty()) {
            cacheKey = "Default key generator";
        }

        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        log.debug("Cache operation on method: {}, caches: {}, key: {}",
                methodName, String.join(",", cacheNames), cacheKey);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.debug("Method {} execution completed in {} ms", methodName, (endTime - startTime));

        return result;
    }
}
