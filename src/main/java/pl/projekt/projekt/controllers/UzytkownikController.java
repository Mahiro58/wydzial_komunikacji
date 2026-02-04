package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.entity.UzytkownikEnt;
import pl.projekt.projekt.repo.UzytkownikRepo;

import java.util.List;

@RestController
@RequestMapping("/uzytkownik")
@CrossOrigin
@Tag(
    name = "Użytkownicy",
    description = "Operacje CRUD na encji Użytkownik"
)
public class UzytkownikController {

    private final UzytkownikRepo repo;

    public UzytkownikController(UzytkownikRepo repo) {
        this.repo = repo;
    }

    // GET /uzytkownik
    @Operation(
        summary = "Pobierz wszystkich użytkowników",
        description = "Zwraca listę wszystkich użytkowników zapisanych w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Lista użytkowników została zwrócona poprawnie")
    @GetMapping
    public List<UzytkownikEnt> getAll() {
        return repo.findAll();
    }

    // POST /uzytkownik
    @Operation(
        summary = "Dodaj nowego użytkownika",
        description = "Tworzy nowego użytkownika w bazie danych na podstawie danych przesłanych w body żądania."
    )
    @ApiResponse(responseCode = "200", description = "Użytkownik został poprawnie zapisany w bazie")
    @PostMapping
    public UzytkownikEnt create(
        @Parameter(
            description = "Obiekt użytkownika do zapisania w bazie danych",
            required = true
        )
        @RequestBody UzytkownikEnt uzytkownik
    ) {
        return repo.save(uzytkownik);
    }

    // DELETE /uzytkownik/{id}
    @Operation(
        summary = "Usuń użytkownika",
        description = "Usuwa użytkownika z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "200", description = "Użytkownik został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika o podanym ID")
    @DeleteMapping("/{id}")
    public void delete(
        @Parameter(
            description = "Identyfikator użytkownika do usunięcia",
            required = true,
            example = "1"
        )
        @PathVariable Long id
    ) {
        repo.deleteById(id);
    }
}
