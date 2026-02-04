package pl.projekt.projekt.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.projekt.projekt.entity.TypWniosku;

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

    @Schema(
        description = "Identyfikator pojazdu, którego dotyczy wniosek",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    public Long pojazdId;

    @Schema(
        description = "Identyfikator urzędnika obsługującego wniosek (opcjonalne)",
        example = "2",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    public Long urzednikId;
}
