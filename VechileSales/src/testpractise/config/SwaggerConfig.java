package com.dizzion.portal.config;

import com.google.common.io.CharStreams;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static springfox.documentation.builders.PathSelectors.any;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

@Configuration
@Import({BeanValidatorPluginsConfiguration.class})
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(basePackage("com.dizzion.portal"))
                .build()
                .produces(newHashSet("application/json"))
                .consumes(newHashSet("application/json"))
                .securitySchemes(newArrayList(apiKey()))
                .securityContexts(newArrayList(securityContext()))
                .genericModelSubstitutes(Optional.class)
                .apiInfo(apiInfo());
    }

    @SneakyThrows
    private ApiInfo apiInfo() {
        return new ApiInfo("Dizzion portal API",
                CharStreams.toString(new InputStreamReader(new ClassPathResource("swagger-documentation.md").getInputStream())),
                "1.0", "", new Contact("DevOps team",
                "", "devops@dizzion.com"), "", "", emptyList());
    }

    private ApiKey apiKey() {
        return new ApiKey("mykey", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(any())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = {new AuthorizationScope("global", "accessEverything")};
        return newArrayList(new SecurityReference("mykey", authorizationScopes));
    }
}
