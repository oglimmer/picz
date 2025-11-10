package de.oglimmer.picz.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@Configuration
public class Beans {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName1 = "OpenID";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName1))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName1,
                                        new SecurityScheme()
                                                .name(securitySchemeName1)
                                                .type(SecurityScheme.Type.OPENIDCONNECT)
                                                .openIdConnectUrl("https://id.oglimmer.de/realms/test/.well-known/openid-configuration")
                                )
                )
                .info(new Info().title("PicZ API").version("3")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
