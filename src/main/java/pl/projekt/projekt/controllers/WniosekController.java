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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.projekt.projekt.controllers.dto.WniosekCreateRequest;
import pl.projekt.projekt.entity.*;
import pl.projekt.projekt.repo.*;
import pl.projekt.projekt.ws.WniosekStatusEvent;

import java.util.List;

@RestController
@RequestMapping("/wniosek")
@CrossOrigin
@Tag(
        name = "Wnioski",
        description = "Obsługa wniosków: tworzenie oraz odczyt danych i zapytań testowych"
)
public class WniosekController {

    private static final Logger log = LogManager.getLogger(WniosekController.class);

    private final WniosekRepo wniosekRepo;
    private final UzytkownikRepo uzytkownikRepo;
    private final PojazdRepo pojazdRepo;
    private final UrzednikRepo urzednikRepo;

    // WebSocket publisher
    private final SimpMessagingTemplate ws;

    public WniosekController(WniosekRepo wniosekRepo,
                             UzytkownikRepo uzytkownikRepo,
                             PojazdRepo pojazdRepo,
                             UrzednikRepo urzednikRepo,
                             SimpMessagingTemplate ws) {
        this.wniosekRepo = wniosekRepo;
        this.uzytkownikRepo = uzytkownikRepo;
        this.pojazdRepo = pojazdRepo;
        this.urzednikRepo = urzednikRepo;
        this.ws = ws;
    }

    @Operation(
            summary = "Pobierz wszystkie wnioski",
            description = "Zwraca listę wszystkich wniosków zapisanych w bazie danych."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków została zwrócona poprawnie")
    @GetMapping
    public ResponseEntity<List<WniosekEnt>> getAll() {
        log.info("GET /wniosek - pobieranie wszystkich wniosków");
        List<WniosekEnt> res = wniosekRepo.findAll();
        log.debug("GET /wniosek - liczba rekordów: {}", res.size());
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Utwórz nowy wniosek",
            description = """
                Tworzy nowy wniosek na podstawie danych przesłanych w body.

                Wymagane powiązania:
                - uzytkownikId musi wskazywać istniejącego użytkownika
                - pojazdId musi wskazywać istniejący pojazd
                - urzednikId jest opcjonalne (może być null)

                Status jest ustawiany po stronie serwera (domyślnie ZLOZONY).
                """
    )
    @ApiResponse(responseCode = "200", description = "Wniosek został zapisany i zwrócony w odpowiedzi")
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika/pojazdu/urzędnika o podanym ID")
    @ApiResponse(responseCode = "409", description = "Konflikt danych (np. naruszenie integralności bazy)")
    @PostMapping
    public ResponseEntity<WniosekEnt> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dane potrzebne do utworzenia wniosku",
                    required = true
            )
            @RequestBody WniosekCreateRequest req
    ) {
        if (req == null) {
            log.warn("POST /wniosek - brak body (req == null)");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak danych wniosku w body");
        }

        // log wejścia + DEBUG
        log.info("POST /wniosek - create uzytkownikId={}, pojazdId={}, urzednikId={}",
                req.uzytkownikId, req.pojazdId, req.urzednikId);
        log.debug("POST /wniosek - typ={}, opis={}", req.typ, safe(req.opis));

        // minimalna walidacja
        if (req.uzytkownikId == null || req.pojazdId == null || req.typ == null) {
            log.warn("POST /wniosek - brakuje wymaganych pól (uzytkownikId/pojazdId/typ)");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pola uzytkownikId, pojazdId oraz typ są wymagane"
            );
        }

        try {
            // 1) użytkownik
            log.debug("POST /wniosek - pobieram użytkownika id={}", req.uzytkownikId);
            UzytkownikEnt u = uzytkownikRepo.findById(req.uzytkownikId)
                    .orElseThrow(() -> {
                        log.warn("POST /wniosek - nie znaleziono użytkownika id={}", req.uzytkownikId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie ma użytkownika id=" + req.uzytkownikId);
                    });

            // 2) pojazd
            log.debug("POST /wniosek - pobieram pojazd id={}", req.pojazdId);
            PojazdEnt p = pojazdRepo.findById(req.pojazdId)
                    .orElseThrow(() -> {
                        log.warn("POST /wniosek - nie znaleziono pojazdu id={}", req.pojazdId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie ma pojazdu id=" + req.pojazdId);
                    });

            // 3) urzędnik
            UrzednikEnt urz = null;
            if (req.urzednikId != null) {
                log.debug("POST /wniosek - pobieram urzędnika id={}", req.urzednikId);
                urz = urzednikRepo.findById(req.urzednikId)
                        .orElseThrow(() -> {
                            log.warn("POST /wniosek - nie znaleziono urzędnika id={}", req.urzednikId);
                            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie ma urzędnika id=" + req.urzednikId);
                        });
            } else {
                log.warn("POST /wniosek - brak urzednikId (wniosek bez przypisanego urzędnika)");
            }

            // 4) budowa encji i zapis
            WniosekEnt w = new WniosekEnt();
            w.setTyp(req.typ);
            w.setOpis(req.opis);
            w.setUzytkownik(u);
            w.setPojazd(p);
            w.setUrzednik(urz);

            WniosekEnt saved = wniosekRepo.save(w);

            log.info("POST /wniosek - zapisano wniosek id={}, status={}, dataZlozenia={}",
                    saved.getId(), saved.getStatus(), saved.getDataZlozenia());

            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("POST /wniosek - naruszenie integralności danych: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zapisać wniosku: konflikt danych lub naruszenie ograniczeń bazy.",
                    e
            );
        } catch (ResponseStatusException e) {
            log.warn("POST /wniosek - błąd klienta: {} {}", e.getStatusCode(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("POST /wniosek - nieoczekiwany błąd: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas tworzenia wniosku", e);
        }
    }

    @Operation(
            summary = "Zmień status wniosku",
            description = "Zmienia status wniosku i publikuje event na WebSocket (/topic/wniosek/events)."
    )
    @ApiResponse(responseCode = "200", description = "Status zmieniony")
    @ApiResponse(responseCode = "400", description = "Niepoprawny status")
    @ApiResponse(responseCode = "404", description = "Nie znaleziono wniosku")
    @ApiResponse(responseCode = "409", description = "Konflikt danych")
    @PatchMapping("/{id}/status")
    public ResponseEntity<WniosekEnt> changeStatus(
            @Parameter(description = "Id wniosku", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nowy status (np. W_TRAKCIE, DO_POPRAWY, ZATWIERDZONY, ODRZUCONY)", required = true)
            @RequestParam("status") String status
    ) {
        log.info("PATCH /wniosek/{}/status - status={}", id, status);

        WniosekEnt w = wniosekRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie ma wniosku id=" + id));

        StatusWniosku newStatus;
        try {
            newStatus = StatusWniosku.valueOf(status);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niepoprawny status: " + status);
        }

        StatusWniosku oldStatus = w.getStatus();
        if (oldStatus == newStatus) {
            log.info("PATCH /wniosek/{}/status - status bez zmian ({}), pomijam event", id, newStatus);
            return ResponseEntity.ok(w);
        }

        w.setStatus(newStatus);

        try {
            WniosekEnt saved = wniosekRepo.save(w);
            log.info("Zmieniono status wniosku id={} z {} na {}", id, oldStatus, newStatus);

            // publikacja eventu po realnym zapisie
            ws.convertAndSend("/topic/wniosek/events", new WniosekStatusEvent(id, oldStatus, newStatus));
            log.info("Wysłano WS event na /topic/wniosek/events dla wniosku id={}", id);

            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error("PATCH /wniosek/{}/status - naruszenie integralności danych: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zmienić statusu wniosku: konflikt danych lub naruszenie ograniczeń bazy.",
                    e
            );
        } catch (Exception e) {
            log.error("PATCH /wniosek/{}/status - nieoczekiwany błąd: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd serwera podczas zmiany statusu", e);
        }
    }

    @Operation(
            summary = "Pobierz wnioski użytkownika",
            description = "Zwraca listę wniosków powiązanych z użytkownikiem o podanym ID (zapytanie po relacji)."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków użytkownika została zwrócona poprawnie")
    @GetMapping("/uzytkownik/{id}")
    public ResponseEntity<List<WniosekEnt>> getByUzytkownik(
            @Parameter(description = "Id użytkownika", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /wniosek/uzytkownik/{} - pobieranie wniosków użytkownika", id);
        List<WniosekEnt> res = wniosekRepo.findByUzytkownikId(id);

        if (res.isEmpty()) {
            log.warn("GET /wniosek/uzytkownik/{} - brak wniosków dla użytkownika", id);
        } else {
            log.debug("GET /wniosek/uzytkownik/{} - liczba wniosków: {}", id, res.size());
        }

        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Pobierz wnioski o statusie 'złożony'",
            description = "Zwraca listę wniosków o statusie 'złożony' z wykorzystaniem native query."
    )
    @ApiResponse(responseCode = "200", description = "Lista wniosków została zwrócona poprawnie")
    @GetMapping("/status/zlozony")
    public ResponseEntity<List<WniosekEnt>> getZlozone() {
        log.info("GET /wniosek/status/zlozony - pobieranie wniosków o statusie ZLOZONY (native query)");
        List<WniosekEnt> res = wniosekRepo.findZlozoneNative();
        log.debug("GET /wniosek/status/zlozony - liczba rekordów: {}", res.size());
        return ResponseEntity.ok(res);
    }

    private static String safe(String s) {
        if (s == null) return null;
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
