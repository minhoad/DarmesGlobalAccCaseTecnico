package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import feign.Feign;
import feign.QueryMapEncoder;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor bcbRequestInterceptor() {
        return template -> {
            log.debug("\n\n[FEIGN REQUEST ORIGINAL] URL: {}\nPARÂMETROS: {}\n",
                    template.feignTarget().url() + template.path(),
                    template.queries());

            if (template.path().contains("CotacaoDolarDia")) {
                Map<String, Collection<String>> fixedQueries = new HashMap<>(template.queries());

                Collection<String> dateParams = fixedQueries.get("@dataCotacao");
                if (dateParams != null && !dateParams.isEmpty()) {
                    template.queries().clear();

                    String dateParam = dateParams.iterator().next();
                    if (!dateParam.startsWith("'") && !dateParam.endsWith("'")) {
                        dateParam = "'" + dateParam + "'";
                    }

                    template.query("@dataCotacao", dateParam);
                    template.query("$top", "100");
                    template.query("$format", "json");

                    log.debug("\n[FEIGN REQUEST AFTER] URL: {}\nPARÂMETROS: {}\n",
                            template.feignTarget().url() + template.path(),
                            template.queries());
                }
            }
        };
    }
}