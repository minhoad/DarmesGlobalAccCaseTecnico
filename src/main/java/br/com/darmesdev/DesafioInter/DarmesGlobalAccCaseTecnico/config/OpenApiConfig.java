package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("case técnico inter");

        Contact contact = new Contact();
        contact.setEmail("darmesdias@gmail.com");
        contact.setName("DarmesDev");
        contact.setUrl("");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://blabla.com/licenses/mit/");

        Info info = new Info()
                .title("Remittance API Documentation")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for managing users, balances, and remittances.")
                .termsOfService("https://www.darmesdev.com.br/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
