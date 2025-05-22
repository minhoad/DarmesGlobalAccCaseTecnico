package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients(clients = BCBClient.class)
@EnableJpaRepositories
@SpringBootApplication
public class DarmesGlobalAccCaseTecnicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarmesGlobalAccCaseTecnicoApplication.class, args);
	}

}
