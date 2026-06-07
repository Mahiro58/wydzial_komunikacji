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
import pl.projekt.projekt.entity.UzytkownikEnt;
import pl.projekt.projekt.repo.UzytkownikRepo;
import pl.projekt.projekt.controllers.dto.AktualizacjaKontaktuRequest;

import java.util.List;

@RestController
@RequestMapping("/uzytkownik")
@CrossOrigin
@Tag(
    name = "Użytkownicy",
    description = "Operacje CRUD na encji Użytkownik"
)
public class UzytkownikController {

    private static final Logger log = LogManager.getLogger(UzytkownikController.class);

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
    public ResponseEntity<List<UzytkownikEnt>> getAll() {
        log.info("GET /uzytkownik - pobieranie wszystkich użytkowników");
        List<UzytkownikEnt> res = repo.findAll();
        log.debug("GET /uzytkownik - liczba rekordów: {}", res.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UzytkownikEnt> getById(@PathVariable Long id) {
        UzytkownikEnt user = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono użytkownika o id=" + id
                ));

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/kontakt")
    public ResponseEntity<UzytkownikEnt> updateContact(
            @PathVariable Long id,
            @RequestBody AktualizacjaKontaktuRequest request
    ) {
        UzytkownikEnt user = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono użytkownika o id=" + id
                ));

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email jest wymagany");
        }

        user.setEmail(request.getEmail());
        user.setTelefon(request.getTelefon());

        UzytkownikEnt saved = repo.save(user);

        return ResponseEntity.ok(saved);
    }

    // POST /uzytkownik
    @Operation(
        summary = "Dodaj nowego użytkownika",
        description = "Tworzy nowego użytkownika w bazie danych na podstawie danych przesłanych w body żądania."
    )
    @ApiResponse(responseCode = "200", description = "Użytkownik został poprawnie zapisany w bazie")
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "409", description = "Konflikt danych (np. naruszenie ograniczeń bazy)")
    @PostMapping
    public ResponseEntity<UzytkownikEnt> create(
        @Parameter(description = "Obiekt użytkownika do zapisania w bazie danych", required = true)
        @RequestBody UzytkownikEnt uzytkownik
    ) {
        if (uzytkownik == null) {
            log.warn("POST /uzytkownik - brak body (uzytkownik == null)");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak danych użytkownika w body");
        }

        log.info("POST /uzytkownik - próba zapisu użytkownika imie={}, nazwisko={}, email={}, telefon={}",
                safe(uzytkownik.getImie()),
                safe(uzytkownik.getNazwisko()),
                safe(uzytkownik.getEmail()),
                safe(uzytkownik.getTelefon())
        );

        if (isBlank(uzytkownik.getImie()) || isBlank(uzytkownik.getNazwisko())) {
            log.warn("POST /uzytkownik - brak wymaganych pól: imie lub nazwisko");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pola 'imie' i 'nazwisko' są wymagane");
        }

        try {
            UzytkownikEnt saved = repo.save(uzytkownik);
            log.info("POST /uzytkownik - zapisano użytkownika id={}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("POST /uzytkownik - naruszenie integralności danych: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zapisać użytkownika: konflikt danych lub naruszenie ograniczeń bazy.",
                    e
            );
        } catch (Exception e) {
            log.error("POST /uzytkownik - nieoczekiwany błąd zapisu: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas zapisu użytkownika", e);
        }
    }

    // DELETE /uzytkownik/{id}
    @Operation(
        summary = "Usuń użytkownika",
        description = "Usuwa użytkownika z bazy danych na podstawie jego identyfikatora."
    )
    @ApiResponse(responseCode = "204", description = "Użytkownik został usunięty")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika o podanym ID")
    @ApiResponse(responseCode = "409", description = "Nie można usunąć użytkownika (np. jest powiązany z wnioskami)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identyfikator użytkownika do usunięcia", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("DELETE /uzytkownik/{} - próba usunięcia użytkownika", id);

        if (!repo.existsById(id)) {
            log.warn("DELETE /uzytkownik/{} - nie znaleziono użytkownika", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono użytkownika o id=" + id);
        }

        try {
            repo.deleteById(id);
            log.info("DELETE /uzytkownik/{} - usunięto użytkownika", id);
            return ResponseEntity.noContent().build(); // 204

        } catch (DataIntegrityViolationException e) {
            log.error("DELETE /uzytkownik/{} - nie można usunąć (integrity violation): {}", id, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można usunąć użytkownika, ponieważ jest powiązany z innymi rekordami.",
                    e
            );
        } catch (Exception e) {
            log.error("DELETE /uzytkownik/{} - nieoczekiwany błąd usuwania: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas usuwania użytkownika", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        if (s == null) return null;
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }
}
