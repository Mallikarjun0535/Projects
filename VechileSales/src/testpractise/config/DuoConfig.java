package com.dizzion.portal.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;

import static com.dizzion.portal.domain.common.DateUtils.getCurrentDateInRfc2822;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.apache.commons.codec.digest.HmacUtils.hmacSha1Hex;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.DATE;

@Configuration
public class DuoConfig {

    private final String integrationToken;
    private final String hostname;
    private final String secret;

    public DuoConfig(@Value("${duo.integration-token}") String integrationToken,
                     @Value("${duo.hostname}") String hostname,
                     @Value("${duo.secret}") String secret) {
        this.integrationToken = integrationToken;
        this.hostname = hostname;
        this.secret = secret;
    }

    @Bean
    @Qualifier("duoRestTemplate")
    public RestTemplate duoRestTemplate(MappingJackson2HttpMessageConverter jacksonConverter) {
        ClientHttpRequestInterceptor clientHttpRequestInterceptor = (request, body, execution) -> {
            Map<String, String> params = extractParams(request, body);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("");
            params.entrySet().stream()
                    .sorted(comparing(Map.Entry::getKey))
                    .forEach(entry -> uriBuilder.queryParam(entry.getKey(), entry.getValue()));
            String formattedParams = uriBuilder.build().getQuery();

            String date = getCurrentDateInRfc2822();
            String formattedRequest = String.format("%s\n%s\n%s\n%s\n%s",
                    date, request.getMethod(), hostname, request.getURI().getPath(), formattedParams);

            String password = hmacSha1Hex(secret, formattedRequest);
            String authValue = Base64.getEncoder().encodeToString((integrationToken + ":" + password).getBytes());
            request.getHeaders().set(AUTHORIZATION, "Basic " + authValue);
            request.getHeaders().set(DATE, date);
            return execution.execute(request, body);
        };

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60 * 1000);
        requestFactory.setReadTimeout(60 * 1000);
        RestTemplate restTemplate = new RestTemplate(new InterceptingClientHttpRequestFactory(
                requestFactory, asList(clientHttpRequestInterceptor)));
        restTemplate.setMessageConverters(asList(new FormHttpMessageConverter(), jacksonConverter));
        return restTemplate;
    }

    private Map<String, String> extractParams(HttpRequest request, byte[] body) {
        return UriComponentsBuilder.fromUri(request.getURI()).query(new String(body))
                .build().getQueryParams().toSingleValueMap();
    }
}
