package pl.projekt.projekt.controllers.dto;

import pl.projekt.projekt.entity.TypWniosku;

public class WniosekCreateRequest {
    public TypWniosku typ;
    public String opis;
    public Long uzytkownikId;
    public Long pojazdId;
    public Long urzednikId;
}
