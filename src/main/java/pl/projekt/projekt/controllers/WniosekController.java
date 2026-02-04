package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.controllers.dto.WniosekCreateRequest;
import pl.projekt.projekt.entity.*;
import pl.projekt.projekt.repo.*;

import java.util.List;

@RestController
@RequestMapping("/wniosek")
@CrossOrigin
@Tag(
    name = "Wnioski",
    description = "Obsługa wniosków: tworzenie oraz odczyt danych i zapytań testowych"
)
public class WniosekController {

    private final WniosekRepo wniosekRepo;
    private final UzytkownikRepo uzytkownikRepo;
    private final PojazdRepo pojazdRepo;
    private final UrzednikRepo urzednikRepo;

    public WniosekController(WniosekRepo wniosekRepo,
                             UzytkownikRepo uzytkownikRepo,
                             PojazdRepo pojazdRepo,
                             UrzednikRepo urzednikRepo) {
        this.wniosekRepo = wniosekRepo;
        this.uzytkownikRepo = uzytkownikRepo;
        this.pojazdRepo = pojazdRepo;
        this.urzednikRepo = urzednikRepo;
    }

    @Operation(
        summary = "Pobierz wszystkie wnioski",
        description = "Zwraca listę wszystkich wniosków zapisanych w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków została zwrócona poprawnie")
    @GetMapping
    public List<WniosekEnt> getAll() {
        return wniosekRepo.findAll();
    }

    @Operation(
        summary = "Utwórz nowy wniosek",
        description = """
            Tworzy nowy wniosek na podstawie danych przesłanych w body.
            
            Wymagane powiązania:
            - uzytkownikId musi wskazywać istniejącego użytkownika
            - pojazdId musi wskazywać istniejący pojazd
            - urzednikId jest opcjonalne (może być null)
            """
    )
    @ApiResponse(responseCode = "200", description = "Wniosek został zapisany i zwrócony w odpowiedzi")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika/pojazdu/urzędnika o podanym ID")
    @PostMapping
    public WniosekEnt create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dane potrzebne do utworzenia wniosku",
            required = true
        )
        @RequestBody WniosekCreateRequest req
    ) {

        UzytkownikEnt u = uzytkownikRepo.findById(req.uzytkownikId)
                .orElseThrow(() -> new RuntimeException("Nie ma uzytkownika id=" + req.uzytkownikId));

        PojazdEnt p = pojazdRepo.findById(req.pojazdId)
                .orElseThrow(() -> new RuntimeException("Nie ma pojazdu id=" + req.pojazdId));

        UrzednikEnt urz = null;
        if (req.urzednikId != null) {
            urz = urzednikRepo.findById(req.urzednikId)
                    .orElseThrow(() -> new RuntimeException("Nie ma urzednika id=" + req.urzednikId));
        }

        WniosekEnt w = new WniosekEnt();
        w.setTyp(req.typ);
        w.setOpis(req.opis);
        w.setUzytkownik(u);
        w.setPojazd(p);
        w.setUrzednik(urz);

        return wniosekRepo.save(w);
    }

    @Operation(
        summary = "Pobierz wnioski użytkownika",
        description = "Zwraca listę wniosków powiązanych z użytkownikiem o podanym ID (zapytanie po relacji)."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków użytkownika została zwrócona poprawnie")
    @GetMapping("/uzytkownik/{id}")
    public List<WniosekEnt> getByUzytkownik(
        @Parameter(description = "Id użytkownika", required = true, example = "1")
        @PathVariable Long id
    ) {
        return wniosekRepo.findByUzytkownikId(id);
    }

    @Operation(
        summary = "Pobierz wnioski o statusie 'złożony'",
        description = "Zwraca listę wniosków o statusie 'złożony' z wykorzystaniem native query."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków została zwrócona poprawnie")
    @GetMapping("/status/zlozony")
    public List<WniosekEnt> getZlozone() {
        return wniosekRepo.findZlozoneNative();
    }
}
