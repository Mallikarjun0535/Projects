package com.dizzion.portal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;
import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
public class ConnectWiseConfig {
    private final long sizeLimit;
    private final String authToken;

    public ConnectWiseConfig(@Value("${connectwise.attachment-size-limit}") long sizeLimit,
                             @Value("${connectwise.authtoken}") String authToken) {
        this.sizeLimit = sizeLimit;
        this.authToken = authToken;
    }

    @Bean
    @Qualifier("connectWiseRestTemplate")
    public RestTemplate connectWiseRestTemplate(MappingJackson2HttpMessageConverter jacksonConverter) {
        ObjectMapper objectMapper = jacksonConverter.getObjectMapper().copy();
        objectMapper.setSerializationInclusion(NON_NULL);
        objectMapper.configure(READ_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);

        RestTemplate restTemplate = new RestTemplate(requestFactory());
        restTemplate.setMessageConverters(asList(
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter(),
                byteArrayHttpMessageConverterWithSizeLimit()
        ));
        return restTemplate;
    }

    private ClientHttpRequestFactory requestFactory() {
        return new HttpComponentsClientHttpRequestFactory(
                HttpClients.custom()
                        .setDefaultHeaders(asList(
                                new BasicHeader(ACCEPT, APPLICATION_JSON_VALUE),
                                new BasicHeader(AUTHORIZATION, "Basic " + authToken)
                        ))
                        .build()
        );
    }

    private ByteArrayHttpMessageConverter byteArrayHttpMessageConverterWithSizeLimit() {
        return new ByteArrayHttpMessageConverter() {
            @Override
            public byte[] readInternal(Class<? extends byte[]> clazz, HttpInputMessage inputMessage) throws IOException {
                long contentLength = inputMessage.getHeaders().getContentLength();
                if (contentLength > sizeLimit) {
                    throw new MaxUploadSizeExceededException(sizeLimit);
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream(contentLength >= 0 ? (int) contentLength : StreamUtils.BUFFER_SIZE);
                long bytesRead = StreamUtils.copyRange(inputMessage.getBody(), bos, 0, sizeLimit - 1);
                if (bytesRead == sizeLimit) {
                    throw new MaxUploadSizeExceededException(sizeLimit);
                }
                return bos.toByteArray();
            }
        };
    }
}
