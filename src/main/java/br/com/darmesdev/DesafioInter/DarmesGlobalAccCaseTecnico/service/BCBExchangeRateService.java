package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.ExchangeRateUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Primary
@RequiredArgsConstructor
public class BCBExchangeRateService implements ExchangeRateStrategy {

    private final BCBClient bcbClient;
    private final ExchangeRateRepository repository;

    @Override
    @CircuitBreaker(name = "bcbService", fallbackMethod = "getLastAvailableRate")
    public BigDecimal getExchangeRate(LocalDate date) {
        if (isWeekend(date)) {
            return getLastWeekdayRate();
        }

        return repository.findByDate(date)
                .orElseGet(() -> fetchAndSaveRate(date))
                .getRate();
    }

    private ExchangeRate fetchAndSaveRate(LocalDate date) {
        BCBClient.CotacaoResponse response = bcbClient.getCotacaoDia(
                formatDate(date),
                1,
                "json"
        );
        return processResponse(response, date);
    }

    private ExchangeRate processResponse(BCBClient.CotacaoResponse response, LocalDate date) {
        return response.cotacoes().stream()
                .findFirst()
                .map(c -> {
                    ExchangeRate rate = new ExchangeRate(date, c.compra());
                    return repository.save(rate);
                })
                .orElseThrow(ExchangeRateUnavailableException::new);
    }

    private BigDecimal getLastWeekdayRate() {
        return repository.findTopByOrderByDateDesc()
                .map(ExchangeRate::getRate)
                .orElseThrow(ExchangeRateUnavailableException::new);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }
}