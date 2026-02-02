package pl.projekt.projekt.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class PierwszyController {

    @GetMapping("/hello")
    public String hello() {
        return "Udało się sprawdzić pierwszy endpoint";
    }
    
}
