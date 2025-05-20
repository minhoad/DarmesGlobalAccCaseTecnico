package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;

public class MonitorConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metrics() {
        return registry -> registry.config().commonTags("application", "darmes-global-acc");
    }
}
