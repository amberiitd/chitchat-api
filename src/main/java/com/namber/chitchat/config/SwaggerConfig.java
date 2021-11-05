package com.namber.chitchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spi.service.contexts.SecurityContextBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(getSecuritySchemes())
                .securityContexts(getSecurityContext())
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.any())
                .build();
    }

    private List<SecurityContext> getSecurityContext() {
        return Arrays.asList(
            new SecurityContextBuilder().securityReferences(
                    Arrays.asList(
                            new SecurityReference("Basic Auth",
                                    new AuthorizationScope[]{
                                            new AuthorizationScope("read", "fetch user data")
                            })
                    )
            ).build()
        );
    }

    private List<SecurityScheme> getSecuritySchemes() {
        return Arrays.asList(
                new BasicAuth("Basic Auth")
        );
    }
}
