package pl.projekt.projekt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjektApplication {

    private static final Logger log = LogManager.getLogger(ProjektApplication.class);

    public static void main(String[] args) {
        log.info("Witaj w aplikacji do rejestracji pojazdów.");
        log.info("W tej aplikacji możesz zarejestrować pojazd lub zgłosić jego sprzedaż.");
        SpringApplication.run(ProjektApplication.class, args);
    }
}
