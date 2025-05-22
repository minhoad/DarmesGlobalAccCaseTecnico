package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.ExchangeRateUnavailableException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.FutureDateException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;


@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BCBExchangeRateService implements ExchangeRateStrategy {

    // Usando o cliente direto com WebClient
    private final BCBDirectClient bcbDirectClient;
    private final ExchangeRateRepository repository;

    @Override
    @Cacheable(value = "exchangeRates", key = "#date.toString()", unless = "#result.isEmpty()")
    @CircuitBreaker(name = "bcbService", fallbackMethod = "getLastAvailableRate")
    public Optional<BigDecimal> getExchangeRate(LocalDate date) {
        log.info("Fetching exchange rate for date: {} (not from cache)", date);

        if (date.isAfter(LocalDate.now())) {
            throw new FutureDateException("Data futura não suportada: " + date);
        }
        if (isWeekend(date)) {
            return getLastWeekdayRate(date)
                    .or(() -> getLastAvailableRate(date, new ExchangeRateUnavailableException()));
        }

        return repository.findByDate(date)
                .map(ExchangeRate::getRate)
                .or(() -> fetchNewRate(date));
    }

    private Optional<BigDecimal> fetchNewRate(LocalDate date) {
        try {
            log.info("Buscando cotação do dólar para o dia: {}", date);

            // Usando o cliente direto com WebClient
            BCBClient.CotacaoResponse response = bcbDirectClient.getCotacaoDia(date);

            return processApiResponse(response, date);
        } catch (WebClientResponseException e) {
            log.error("Requisição inválida para BCB: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            log.warn("Buscando última taxa válida devido a: cotação indisponivel para o dia: {}", date);
            throw new ExchangeRateUnavailableException(date);
        } catch (Exception e) {
            log.error("Falha na comunicação com BCB: {}", e.getMessage());
            log.warn("Buscando última taxa válida devido a: cotação indisponivel para o dia: {}", date);
            throw new ExchangeRateUnavailableException(date);
        }
    }

    private Optional<BigDecimal> processApiResponse(BCBClient.CotacaoResponse response, LocalDate requestedDate) {
        if (response.cotacoes() == null || response.cotacoes().isEmpty()) {
            log.info("Nenhuma cotação encontrada para {}", requestedDate);
            handleMissingRate(requestedDate);
            return Optional.empty();
        }

        return response.cotacoes().stream()
                .findFirst()
                .map(cotacao -> {
                    // 1. Validar campos obrigatórios
                    if (cotacao.compra() == null) {
                        log.warn("Cotação sem valor para data {}", requestedDate);
                        return Optional.<BigDecimal>empty();
                    }

                    // 2. Extrair data REAL da cotação
                    LocalDate effectiveDate = cotacao.data().toLocalDate();

                    // 3. Verificar consistência da data
                    if (!effectiveDate.equals(requestedDate)) {
                        log.warn("Data divergente: solicitada {} vs resposta {}", requestedDate, effectiveDate);
                        throw new FutureDateException("Data da cotação no futuro: " + effectiveDate);
                    }

                    // 4. Evitar duplicação com controle concorrencial
                    return repository.findByDate(effectiveDate)
                            .or(() -> {
                                ExchangeRate newRate = new ExchangeRate(effectiveDate, cotacao.compra());
                                try {
                                    return Optional.of(repository.save(newRate));
                                } catch (DataIntegrityViolationException e) {
                                    // Race condition tratada: outro thread já inseriu
                                    return repository.findByDate(effectiveDate);
                                }
                            })
                            .map(ExchangeRate::getRate);
                })
                .orElseGet(() -> {
                    // 5. Registrar ausência de cotação explicitamente
                    log.info("Nenhuma cotação encontrada para {}", requestedDate);
                    handleMissingRate(requestedDate);
                    return Optional.empty();
                });
    }

    private void handleMissingRate(LocalDate date) {
        try {
            repository.save(new ExchangeRate(date));
            log.info("Registrada ausência de cotação para {}", date);
        } catch (DataIntegrityViolationException e) {
            log.debug("Registro de ausência já existente para {}", date);
        }
    }

    private Optional<BigDecimal> getLastWeekdayRate(LocalDate date) {
        return repository.findLatestUntilDate(date.minusDays(1))
                .map(ExchangeRate::getRate);
    }

    public Optional<BigDecimal> getLastAvailableRate(LocalDate date, Throwable t) {
        log.warn("Buscando última taxa válida devido a: {}", t.getMessage());

        return repository.findLatestValidRateBefore(date)
                .or(() -> repository.findLatestValidRateBefore(LocalDate.now().minusDays(7)))
                .map(ExchangeRate::getRate);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}