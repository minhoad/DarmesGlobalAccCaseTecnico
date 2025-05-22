package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBDirectClient;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBExchangeRateService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.RemittanceService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {
    @Bean
    @Primary
    public RemittanceService remittanceService() {
        return Mockito.mock(RemittanceService.class);
    }

}
