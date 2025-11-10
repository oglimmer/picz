package de.oglimmer.picz.config;

import de.oglimmer.picz.db.UserRepository;
import de.oglimmer.picz.service.UserRepositoryService;
import de.oglimmer.picz.util.UserInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private UserRepository userRepository;
    private UserRepositoryService userRepositoryService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor(userRepository, userRepositoryService))
                .addPathPatterns("/**")
                .excludePathPatterns("/api/public/**");
    }
}
