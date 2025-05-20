package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "bcb-client", url = "${api.bcb.base-url}")
public interface BCBClient {

    @GetMapping(value = "/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)",
            consumes = "application/json")
    CotacaoResponse getCotacaoDia(
            @RequestParam("@dataCotacao") String dataCotacao,
            @RequestParam("$top") int top,
            @RequestParam("$format") String format
    );

    record CotacaoResponse(
            @JsonProperty("value") List<Cotacao> cotacoes
    ) {
        record Cotacao(
                @JsonProperty("cotacaoCompra") BigDecimal compra,
                @JsonProperty("dataHoraCotacao") LocalDateTime data
        ) {}
    }
}