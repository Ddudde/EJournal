package ru.mirea.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class ConfigMVC implements WebMvcConfigurer {
    private final GsonHttpMessageConverter converter = new GsonHttpMessageConverter();

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(converter);
    }
}