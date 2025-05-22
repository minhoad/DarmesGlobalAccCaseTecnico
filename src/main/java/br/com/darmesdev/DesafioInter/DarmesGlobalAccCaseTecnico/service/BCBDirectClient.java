package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BCBDirectClient {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${api.bcb.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    /**
     * Obtém a cotação do dólar para uma data específica usando WebClient.
     * Isso evita problemas de codificação de URL que podem ocorrer com o Feign.
     * Que raiva desse feign client, fui inventar moda de usar ele sem estar muito acostumado perdi muito tempo
     */
    public BCBClient.CotacaoResponse getCotacaoDia(LocalDate date) {
        String formattedDate = date.format(DATE_FORMATTER);


        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)")
                .queryParam("@dataCotacao", "'" + formattedDate + "'")
                .queryParam("$top", 100)
                .queryParam("$format", "json")
                .build()
                .toUriString();

        log.info("Fazendo requisição para URL: {}", url);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(BCBClient.CotacaoResponse.class)
                .doOnError(error -> {
                    log.error("Erro na requisição BCB: {}", error.getMessage());
                })
                .block();
    }
}
