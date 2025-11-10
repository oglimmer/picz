package de.oglimmer.picz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "picz.scheduler.parallel")
@Getter
@Setter
public class ParallelSchedulerProps {

    private int maxConcurrentJobs = 5;

}