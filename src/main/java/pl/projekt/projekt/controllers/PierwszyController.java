package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PierwszyController {

    @Operation(
        summary = "Endpoint testowy",
        description = "Prosty endpoint GET służący do sprawdzenia, czy aplikacja oraz Swagger działają poprawnie."
    )
    @GetMapping("/hello")
    public String hello() {
        return "Udało się sprawdzić pierwszy endpoint";
    }

}
