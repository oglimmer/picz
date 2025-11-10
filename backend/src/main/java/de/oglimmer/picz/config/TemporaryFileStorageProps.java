package de.oglimmer.picz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "picz.temporary-file-storage")
public class TemporaryFileStorageProps {
    private String host = "localhost";
    private int port = 8081;
    private String bucket = "bucket";
    
    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }
}