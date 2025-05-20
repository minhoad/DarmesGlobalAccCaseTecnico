package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class DarmesGlobalAccCaseTecnicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarmesGlobalAccCaseTecnicoApplication.class, args);
	}

}
