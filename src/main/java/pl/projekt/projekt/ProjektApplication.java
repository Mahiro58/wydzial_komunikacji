package pl.projekt.projekt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjektApplication {

	public static void main(String[] args) {
		System.out.println("W tej aplikacji możesz zarejestrować pojazd lub zgłosić jego sprzedaż.");
		System.out.println("Witaj w aplikacji do rejestracji pojazdów.");
		SpringApplication.run(ProjektApplication.class, args);
	}

}
