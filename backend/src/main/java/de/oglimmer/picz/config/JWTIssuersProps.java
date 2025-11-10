package de.oglimmer.picz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "config")
@Getter
@Setter
class JWTIssuersProps {
    private List<String> issuers;
}
