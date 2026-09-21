package com.tms;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
class WebConfiguration implements WebMvcConfigurer {
    @Override public void configurePathMatch(PathMatchConfigurer configurer) {
        var parser = new PathPatternParser();
        parser.setCaseSensitive(false); // Express routers are case-insensitive by default.
        configurer.setPatternParser(parser);
    }
}
