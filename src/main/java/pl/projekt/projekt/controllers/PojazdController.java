package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.entity.PojazdEnt;
import pl.projekt.projekt.repo.PojazdRepo;

import java.util.List;

@RestController
@RequestMapping("/pojazd")
@CrossOrigin
@Tag(
    name = "Pojazdy",
    description = "Operacje CRUD na encji Pojazd"
)
public class PojazdController {

    private final PojazdRepo pojazdRepo;

    public PojazdController(PojazdRepo pojazdRepo) {
        this.pojazdRepo = pojazdRepo;
    }

    // GET /pojazd
    @Operation(
        summary = "Pobierz wszystkie pojazdy",
        description = "Zwraca listę wszystkich pojazdów zapisanych w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Lista pojazdów została zwrócona poprawnie")
    @GetMapping
    public List<PojazdEnt> getAll() {
        return pojazdRepo.findAll();
    }

    // POST /pojazd
    @Operation(
        summary = "Dodaj nowy pojazd",
        description = "Tworzy nowy pojazd w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Pojazd został poprawnie zapisany w bazie")
    @PostMapping
    public PojazdEnt create(
        @Parameter(description = "Obiekt pojazdu do zapisania w bazie", required = true)
        @RequestBody PojazdEnt pojazd
    ) {
        return pojazdRepo.save(pojazd);
    }

    // DELETE /pojazd/{id}
    @Operation(
        summary = "Usuń pojazd",
        description = "Usuwa pojazd z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "200", description = "Pojazd został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono pojazdu o podanym ID")
    @DeleteMapping("/{id}")
    public void delete(
        @Parameter(description = "Identyfikator pojazdu do usunięcia", required = true, example = "1")
        @PathVariable Long id
    ) {
        pojazdRepo.deleteById(id);
    }
}
