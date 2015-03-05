package com.westbrain.sandbox.spring.config;

/**
 * Created by jruch on 3/2/2015.
 */
import com.westbrain.sandbox.spring.Application;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

public class WebInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }


}