package pl.projekt.projekt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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

    private static final Logger log = LogManager.getLogger(UrzednikController.class);

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
    public ResponseEntity<List<UrzednikEnt>> getAll() {
        log.info("GET /urzednik - pobieranie wszystkich urzędników");
        List<UrzednikEnt> res = repo.findAll();
        log.debug("GET /urzednik - liczba rekordów: {}", res.size());
        return ResponseEntity.ok(res);
    }

    @Operation(
        summary = "Dodaj nowego urzędnika",
        description = "Tworzy nowy rekord urzędnika w bazie danych na podstawie danych przesłanych w body żądania."
    )
    @ApiResponse(responseCode = "200", description = "Urzędnik został poprawnie zapisany w bazie")
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "409", description = "Konflikt danych (np. naruszenie unikalności email)")
    @PostMapping
    public ResponseEntity<UrzednikEnt> create(
        @Parameter(description = "Obiekt urzędnika do zapisania w bazie", required = true)
        @RequestBody UrzednikEnt urzednik
    ) {
        if (urzednik == null) {
            log.warn("POST /urzednik - brak body (urzednik == null)");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak danych urzędnika w body");
        }

        try {
            log.info("POST /urzednik - próba zapisu urzędnika imie={}, nazwisko={}, email={}",
                    safe(urzednik.getImie()),
                    safe(urzednik.getNazwisko()),
                    safe(urzednik.getEmail()));
            log.debug("POST /urzednik - aktywny={}", urzednik.getAktywny());
        } catch (Exception ignore) {
            log.debug("POST /urzednik - log szczegółów pominięty (brak getterów lub inny problem)");
        }

        try {
            UrzednikEnt saved = repo.save(urzednik);
            log.info("POST /urzednik - zapisano urzędnika id={}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("POST /urzednik - naruszenie integralności danych: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zapisać urzędnika: konflikt danych (sprawdź wymagane pola lub unikalność email).",
                    e
            );
        } catch (Exception e) {
            log.error("POST /urzednik - nieoczekiwany błąd zapisu: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas zapisu urzędnika", e);
        }
    }

    @Operation(
        summary = "Usuń urzędnika",
        description = "Usuwa urzędnika z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "204", description = "Urzędnik został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono urzędnika o podanym ID")
    @ApiResponse(responseCode = "409", description = "Nie można usunąć urzędnika (np. jest powiązany z wnioskami)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identyfikator urzędnika do usunięcia", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("DELETE /urzednik/{} - próba usunięcia urzędnika", id);

        if (!repo.existsById(id)) {
            log.warn("DELETE /urzednik/{} - nie znaleziono urzędnika", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono urzędnika o id=" + id);
        }

        try {
            repo.deleteById(id);
            log.info("DELETE /urzednik/{} - usunięto urzędnika", id);
            return ResponseEntity.noContent().build(); // 204

        } catch (DataIntegrityViolationException e) {
            log.error("DELETE /urzednik/{} - nie można usunąć (integrity violation): {}", id, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można usunąć urzędnika, ponieważ jest powiązany z innymi rekordami.",
                    e
            );
        } catch (Exception e) {
            log.error("DELETE /urzednik/{} - nieoczekiwany błąd usuwania: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas usuwania urzędnika", e);
        }
    }

    private static String safe(String s) {
        if (s == null) return null;
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }
}
