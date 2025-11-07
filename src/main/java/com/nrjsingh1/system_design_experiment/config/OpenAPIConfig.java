package com.nrjsingh1.system_design_experiment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setEmail("nrjsingh1@example.com");
        contact.setName("System Design Experiment API");
        contact.setUrl("https://www.example.com");

        License mitLicense = new License()
            .name("MIT License")
            .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
            .title("System Design Experiment API")
            .version("1.0")
            .contact(contact)
            .description("This API exposes endpoints to manage products, customers, and orders.")
            .license(mitLicense);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer));
    }
}