package com.williamfds.integrationhub;

import com.williamfds.integrationhub.infrastructure.config.IntegrationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = IntegrationProperties.class)
public class IntegrationHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationHubApplication.class, args);
    }
}
