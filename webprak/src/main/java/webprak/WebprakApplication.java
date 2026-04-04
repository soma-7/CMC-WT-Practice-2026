package webprak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import webprak.DAO.ClientDAO;

@SpringBootApplication
public class WebprakApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebprakApplication.class, args);
	}

	@Bean
	public CommandLineRunner test(ClientDAO clientDAO) {
		return args -> {
			System.out.println("Clients num: " + clientDAO.getAll().size());
		};
	}
}
