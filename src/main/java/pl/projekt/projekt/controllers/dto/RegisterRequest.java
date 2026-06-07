package pl.projekt.projekt.controllers.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String imie;
    private String nazwisko;
    private String email;
    private String telefon;
    private String haslo;
}