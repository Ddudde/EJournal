package ru.mirea.configs;

//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.servers.Server;
//import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

//    @Bean
//    public OpenAPI myOpenAPI() {
//        Contact contact = new Contact();
//        contact.setEmail("tericcabrel@yahoo.com");
//        contact.setName("Eric Cabrel TIOGO");
//        contact.setUrl("https://my-awesome-api.com");
//
//        Server localServer = new Server();
//        localServer.setUrl("http://localhost:8080/");
//        localServer.setDescription("Server URL in Local environment");
//
//        Server productionServer = new Server();
//        productionServer.setUrl("https://my-awesome-api.com");
//        productionServer.setDescription("Server URL in Production environment");
//
//
//        Info info = new Info()
//                .title("TASK MANAGER API")
//                .contact(contact)
//                .version("1.0")
//                .description("This API exposes endpoints for users to manage their tasks.");
//
//        return new OpenAPI()
//                .info(info)
//                .servers(List.of(localServer, productionServer));
//    }
}
