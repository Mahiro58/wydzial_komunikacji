package pl.projekt.projekt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjektApplication {

	public static void main(String[] args) {
		System.out.println("Zmiana gałęzi");
		SpringApplication.run(ProjektApplication.class, args);
	}

}
