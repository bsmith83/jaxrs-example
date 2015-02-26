package com.westbrain.sandbox.jaxrs.cxf;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.spring.SpringComponentScanServer;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Arrays;

/**
 * Configuration class which configures all of the beans and services required for Apache CXF.
 * <p/>
 * <p>Creates the CXFServlet, the CXF Bus, and creates a Jackson provider for JSON serialization. Also imports
 * the {@link org.apache.cxf.jaxrs.spring.SpringComponentScanServer} which allows for scanning the application context
 * for resources (@Path) and providers (@Provider).</p>
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
@Configuration
@Import(SpringComponentScanServer.class)
public class CxfConfig {

    @Bean
    public ServletRegistrationBean cxfServlet() {
        CXFServlet cxfServlet = new CXFServlet();
        return new ServletRegistrationBean(new CXFServlet(), "/api/v1/*");
    }

    @Bean
    public Bus cxf() {
        return new SpringBus();
    }

    @Bean
    public JacksonJsonProvider jsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    public ResourceListingProvider resourceListingProvider() {
        return new ResourceListingProvider();
    }

    @Bean
    public ApiDeclarationProvider apiDeclarationProvider() {
        return new ApiDeclarationProvider();
    }

    @Bean
    public ApiListingResourceJSON apiListingResourceJSON() {
        return new ApiListingResourceJSON();
    }

    @Bean
    public JaxRsApiApplication jaxRsApiApplication() {
        return new JaxRsApiApplication();
    }

    @Bean(name = "swaggerConfig")
    @Autowired
    public BeanConfig swaggerConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage("com.westbrain.sandbox.jaxrs.group");
        beanConfig.setBasePath("http://localhost:8080/api/v1");
        beanConfig.setVersion("1.0.0");
        beanConfig.setScan(true);
        return beanConfig;
    }

    @Bean
    @DependsOn("cxf")
    public Server jaxrsServerFactoryBean() {
        JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint(jaxRsApiApplication(), JAXRSServerFactoryBean.class);
        factory.setServiceBeans(Arrays.<Object>asList(apiListingResourceJSON()));
        factory.setAddress(factory.getAddress());
        factory.setProviders(Arrays.<Object>asList(jsonProvider(), resourceListingProvider(), apiDeclarationProvider()));
        return factory.create();
    }

    @ApplicationPath("api")
    public class JaxRsApiApplication extends Application {
    }
}
