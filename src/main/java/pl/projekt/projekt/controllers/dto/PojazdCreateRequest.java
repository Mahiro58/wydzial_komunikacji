package pl.projekt.projekt.controllers.dto;

import lombok.Data;

@Data
public class PojazdCreateRequest {
    public String vin;
    public String marka;
    public String model;
    public Short rok;
    public String numerRejestracyjny;
    public Long uzytkownikId;
}