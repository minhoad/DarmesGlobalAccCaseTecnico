package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    private boolean enabled = true;

    private boolean fallbackToLocal = true;

    private Duration defaultTtl = Duration.ofMinutes(30);

    private Map<String, Duration> ttls = new HashMap<>();

    private boolean enableStatistics = true;

    private String keyPrefix = "";

    private String redisHost = "localhost";

    private int redisPort = 6379;

    private String redisPassword = "";

    private long redisTimeout = 2000;

}
