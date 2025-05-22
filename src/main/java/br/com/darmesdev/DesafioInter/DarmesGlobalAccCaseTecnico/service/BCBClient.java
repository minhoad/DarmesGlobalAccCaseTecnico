package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config.FeignConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "bcb-client",
        url = "${api.bcb.base-url}",
        configuration = FeignConfig.class)
public interface BCBClient {

    @GetMapping(value = "/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)",
            consumes = "application/json")
    CotacaoResponse getCotacaoDia(
            @RequestParam(value = "@dataCotacao") String dataCotacao,
            @RequestParam(value = "$top") int top,
            @RequestParam(value = "$format") String format
    );

    record CotacaoResponse(
            @JsonProperty("value") List<Cotacao> cotacoes
    ) {
        record Cotacao(
                @JsonProperty("cotacaoCompra") @NotNull BigDecimal compra,
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SS")
                @JsonProperty("dataHoraCotacao") LocalDateTime data
        ) {}
    }
}