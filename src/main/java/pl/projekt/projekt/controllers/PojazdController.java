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

// ⬇️ ten import dopasuj do Twojej klasy-klienta WebClient
import pl.projekt.projekt.external.VpicClient;
import pl.projekt.projekt.external.dto.VpicDecodeResponse;

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

    // klient do zewnętrznej usługi (WebClient)
    private final VpicClient vpicClient;

    public PojazdController(PojazdRepo pojazdRepo, VpicClient vpicClient) {
        this.pojazdRepo = pojazdRepo;
        this.vpicClient = vpicClient;
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

        // Szybszy i czytelny konflikt VIN zanim dojdziemy do bazy
        if (pojazd.getVin() == null || pojazd.getVin().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VIN jest wymagany");
        }
        if (pojazdRepo.existsByVin(pojazd.getVin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pojazd o VIN już istnieje: " + pojazd.getVin());
        }

        try {
            log.info("POST /pojazd - próba zapisu pojazdu marka={}, model={}, rok={}, vin={}, numerRejestracyjny={}",
                    safe(pojazd.getMarka()),
                    safe(pojazd.getModel()),
                    pojazd.getRok(),
                    safe(pojazd.getVin()),
                    safe(pojazd.getNumerRejestracyjny())
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

    // POST /pojazd/import
    @Operation(
            summary = "Importuj pojazd po VIN z usługi zewnętrznej i zapisz w bazie",
            description = "Wywołuje zewnętrzne API (np. NHTSA vPIC), pobiera markę/model/rok na podstawie VIN i zapisuje pojazd w bazie."
    )
    @ApiResponse(responseCode = "200", description = "Pojazd zaimportowany i zapisany")
    @ApiResponse(responseCode = "400", description = "Brak lub błędny VIN")
    @ApiResponse(responseCode = "409", description = "VIN już istnieje w bazie")
    @ApiResponse(responseCode = "502", description = "Błąd usługi zewnętrznej / brak danych")
    @PostMapping("/import")
    public ResponseEntity<PojazdEnt> importFromExternal(
            @RequestParam("vin") String vin,
            @RequestParam(value = "numerRejestracyjny", required = false) String numerRejestracyjny
    ) {
        log.info("POST /pojazd/import - vin={}, numerRejestracyjny={}", safe(vin), safe(numerRejestracyjny));

        if (vin == null || vin.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametr vin jest wymagany");
        }
        if (vin.trim().length() != 17) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VIN musi mieć 17 znaków");
        }

        if (pojazdRepo.existsByVin(vin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pojazd o VIN już istnieje: " + vin);
        }

        VpicDecodeResponse decoded = vpicClient.decodeVinValues(vin);
        if (decoded == null || decoded.getResults() == null || decoded.getResults().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Brak danych z usługi zewnętrznej dla vin=" + vin);
        }

        VpicDecodeResponse.Result r = decoded.getResults().get(0);

        String marka = r.getMake();
        String model = r.getModel();
        Short rok = parseYearToShort(r.getModelYear());

        if (marka == null || marka.isBlank() || model == null || model.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Usługa zewnętrzna zwróciła niekompletne dane (marka/model)");
        }

        PojazdEnt p = new PojazdEnt();
        p.setVin(vin);
        p.setMarka(marka);
        p.setModel(model);
        p.setRok(rok);
        p.setNumerRejestracyjny(numerRejestracyjny);

        try {
            PojazdEnt saved = pojazdRepo.save(p);
            log.info("POST /pojazd/import - zapisano pojazd id={}, vin={}", saved.getId(), saved.getVin());
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("POST /pojazd/import - konflikt danych: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Konflikt danych przy zapisie pojazdu", e);
        } catch (Exception e) {
            log.error("POST /pojazd/import - błąd zapisu: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas zapisu importu", e);
        }
    }

    private static Short parseYearToShort(String year) {
        try {
            if (year == null || year.isBlank()) return null;
            int y = Integer.parseInt(year.trim());
            if (y < 1900 || y > 3000) return null;
            return (short) y;
        } catch (Exception e) {
            return null;
        }
    }

    private static String safe(String s) {
        if (s == null) return null;
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }
}
