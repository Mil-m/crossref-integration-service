package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient crossrefWebClient(
            @Value("${crossref.base-url:https://api.crossref.org}") String baseUrl,
            @Value("${crossref.mailto:youremail@example.com}") String mailto
    ) {
        String ua = "CrossrefLookup/1.0 (+mailto:" + mailto + ")";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", ua)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
                        .build())
                .build();
    }
}
