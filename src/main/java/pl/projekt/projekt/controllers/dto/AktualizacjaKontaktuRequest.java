package pl.projekt.projekt.controllers.dto;

import lombok.Data;

@Data
public class AktualizacjaKontaktuRequest {
    private String email;
    private String telefon;
}