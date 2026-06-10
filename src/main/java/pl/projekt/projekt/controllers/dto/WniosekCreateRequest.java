package pl.projekt.projekt.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.projekt.projekt.entity.TypWniosku;

import java.time.LocalDate;

@Schema(
        name = "WniosekCreateRequest",
        description = "Dane wejściowe potrzebne do utworzenia nowego wniosku"
)
public class WniosekCreateRequest {

    @Schema(
            description = "Typ wniosku",
            example = "REJESTRACJA",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    public TypWniosku typ;

    @Schema(
            description = "Opis wniosku",
            example = "Rejestracja pojazdu sprowadzonego z zagranicy"
    )
    public String opis;

    @Schema(
            description = "Identyfikator użytkownika składającego wniosek",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    public Long uzytkownikId;

    public Long pojazdId;

    public Long urzednikId;


    public String vin;
    public String marka;
    public String model;
    public Short rok;
    public String numerRejestracyjny;

    public String rodzajPojazdu;
    public String przeznaczenie;

    public LocalDate dataNabycia;

    public String typTablic;
    public Boolean zachowajNumer;
    public String numerIndywidualny;
    public Integer kwotaOplaty;
    public Boolean oplacono;
}