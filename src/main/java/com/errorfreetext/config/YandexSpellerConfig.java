package com.errorfreetext.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * Configuration properties for Yandex Speller API.
 * Automatically populated from application properties with prefix "yandex.speller".
 * Supports environment variable override via ${YANDEX_SPELLER_API_URL} syntax.
 */
@Configuration
@ConfigurationProperties(prefix = "yandex.speller")
@Data
public class YandexSpellerConfig {

    /**
     * Yandex Speller API endpoint URL.
     * Can be overridden via YANDEX_SPELLER_API_URL environment variable.
     * Default: <a href="https://speller.yandex.net/services/spellservice.json/checkTexts">...</a>
     */
    private String apiUrl = "https://speller.yandex.net/services/spellservice.json/checkTexts";

    /**
     * Maximum chunk size for splitting large texts (Yandex API limit: 10,000 characters).
     * Default: 10000
     */
    private int maxChunkSize = 10000;

    /**
     * Connection timeout in milliseconds.
     * Default: 5000
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     * Default: 10000
     */
    private int readTimeout = 10000;

    /**
     * Maximum number of retry attempts when API calls fail.
     * Default: 3
     */
    private int maxAttempts = 3;

    /**
     * Delay between retry attempts in milliseconds.
     * Default: 1000
     */
    private long retryDelay = 1000;

    /**
     * Creates a configured RestTemplate for Yandex Speller API calls.
     * Uses timeouts from configuration properties.
     *
     * @return RestTemplate with configured timeout and base URL
     */
    @Bean
    public RestTemplate yandexRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(apiUrl));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    /**
     * Creates a properties record with core Yandex Speller settings.
     * Used by services that need these values.
     *
     * @return record containing maxChunkSize, maxAttempts, and retryDelay
     */
    @Bean
    public YandexSpellerProperties yandexSpellerProperties() {
        return new YandexSpellerProperties(
                maxChunkSize,
                maxAttempts,
                retryDelay
        );
    }

    /**
     * Immutable record containing core Yandex Speller settings.
     */
    public record YandexSpellerProperties(
            int maxChunkSize,
            int maxAttempts,
            long retryDelay
    ) {
    }
}