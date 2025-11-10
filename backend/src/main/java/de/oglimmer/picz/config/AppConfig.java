package de.oglimmer.picz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class AppConfig {

    @Value("${picz.originalImagePath}")
    private String originalImagePath;

    @Value("${picz.imagePath}")
    private String imagePath;

    @Value("${picz.smallImagePath}")
    private String smallImagePath;

    @Value("${picz.s3Bucket}")
    private String s3Bucket;
    @Value("${picz.s3ImagePath}")
    private String s3ImagePath;
    @Value("${picz.s3SmallImagePath}")
    private String s3SmallImagePath;

}
