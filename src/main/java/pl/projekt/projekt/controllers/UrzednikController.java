package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.entity.UrzednikEnt;
import pl.projekt.projekt.repo.UrzednikRepo;

import java.util.List;

@RestController
@RequestMapping("/urzednik")
@CrossOrigin
@Tag(
    name = "Urzędnicy",
    description = "Operacje CRUD na encji Urzędnik"
)
public class UrzednikController {

    private final UrzednikRepo repo;

    public UrzednikController(UrzednikRepo repo) {
        this.repo = repo;
    }

    @Operation(
        summary = "Pobierz wszystkich urzędników",
        description = "Zwraca listę wszystkich urzędników zapisanych w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Lista urzędników została zwrócona poprawnie")
    @GetMapping
    public List<UrzednikEnt> getAll() {
        return repo.findAll();
    }

    @Operation(
        summary = "Dodaj nowego urzędnika",
        description = "Tworzy nowy rekord urzędnika w bazie danych na podstawie danych przesłanych w body żądania."
    )
    @ApiResponse(responseCode = "200", description = "Urzędnik został poprawnie zapisany w bazie")
    @PostMapping
    public UrzednikEnt create(
        @Parameter(description = "Obiekt urzędnika do zapisania w bazie", required = true)
        @RequestBody UrzednikEnt urzednik
    ) {
        return repo.save(urzednik);
    }

    @Operation(
        summary = "Usuń urzędnika",
        description = "Usuwa urzędnika z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "200", description = "Urzędnik został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono urzędnika o podanym ID")
    @DeleteMapping("/{id}")
    public void delete(
        @Parameter(description = "Identyfikator urzędnika do usunięcia", required = true, example = "1")
        @PathVariable Long id
    ) {
        repo.deleteById(id);
    }
}
