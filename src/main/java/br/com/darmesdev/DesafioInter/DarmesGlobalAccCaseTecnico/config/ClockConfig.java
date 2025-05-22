package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
// `Clock` é uma classe do Java Time API e não é um bean gerenciado pelo Spring por padrão
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}