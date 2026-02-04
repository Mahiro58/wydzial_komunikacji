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

    private static final Logger log = LogManager.getLogger(PojazdController.class);

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
    public ResponseEntity<List<PojazdEnt>> getAll() {
        log.info("GET /pojazd - pobieranie wszystkich pojazdów");
        List<PojazdEnt> res = pojazdRepo.findAll();
        log.debug("GET /pojazd - liczba rekordów: {}", res.size());
        return ResponseEntity.ok(res);
    }

    // POST /pojazd
    @Operation(
        summary = "Dodaj nowy pojazd",
        description = "Tworzy nowy pojazd w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Pojazd został poprawnie zapisany w bazie")
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "409", description = "Konflikt danych (np. naruszenie unikalności VIN / numeru rejestracyjnego)")
    @PostMapping
    public ResponseEntity<PojazdEnt> create(
        @Parameter(description = "Obiekt pojazdu do zapisania w bazie", required = true)
        @RequestBody PojazdEnt pojazd
    ) {
        if (pojazd == null) {
            log.warn("POST /pojazd - brak body (pojazd == null)");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak danych pojazdu w body");
        }

        try {
            log.info("POST /pojazd - próba zapisu pojazdu marka={}, model={}, rok={}, vin={}, numerRejestracyjny={}",
                    safe(get(pojazd.getMarka())),
                    safe(get(pojazd.getModel())),
                    pojazd.getRok(),
                    safe(get(pojazd.getVin())),
                    safe(get(pojazd.getNumerRejestracyjny()))
            );
        } catch (Exception ignore) {
            log.debug("POST /pojazd - log szczegółów pominięty (brak getterów lub inny problem)");
        }

        try {
            PojazdEnt saved = pojazdRepo.save(pojazd);
            log.info("POST /pojazd - zapisano pojazd id={}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("POST /pojazd - naruszenie integralności danych: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zapisać pojazdu: konflikt danych (sprawdź VIN / numer rejestracyjny lub wymagane pola).",
                    e
            );
        } catch (Exception e) {
            log.error("POST /pojazd - nieoczekiwany błąd zapisu pojazdu: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas zapisu pojazdu", e);
        }
    }

    // DELETE /pojazd/{id}
    @Operation(
        summary = "Usuń pojazd",
        description = "Usuwa pojazd z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "204", description = "Pojazd został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono pojazdu o podanym ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identyfikator pojazdu do usunięcia", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("DELETE /pojazd/{} - próba usunięcia pojazdu", id);

        if (!pojazdRepo.existsById(id)) {
            log.warn("DELETE /pojazd/{} - nie znaleziono pojazdu", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono pojazdu o id=" + id);
        }

        try {
            pojazdRepo.deleteById(id);
            log.info("DELETE /pojazd/{} - usunięto pojazd", id);
            return ResponseEntity.noContent().build(); // 204

        } catch (DataIntegrityViolationException e) {
            // Pojazd jest referencjonowany przez wniosek (FK) i nie da się go usunąć
            log.error("DELETE /pojazd/{} - nie można usunąć (integrity violation): {}", id, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można usunąć pojazdu, ponieważ jest powiązany z innymi rekordami.",
                    e
            );
        } catch (Exception e) {
            log.error("DELETE /pojazd/{} - nieoczekiwany błąd usuwania: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas usuwania pojazdu", e);
        }
    }

    private static String safe(String s) {
        if (s == null) return null;
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }

    private static String get(String s) {
        return s;
    }
}
