package pl.projekt.projekt.controllers;

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

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/wniosek")
@CrossOrigin
@Tag(name = "Wnioski")
public class WniosekController {

    private static final Logger log = LogManager.getLogger(WniosekController.class);

    private final WniosekRepo wniosekRepo;
    private final UzytkownikRepo uzytkownikRepo;
    private final PojazdRepo pojazdRepo;
    private final UrzednikRepo urzednikRepo;
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

    @GetMapping
    public ResponseEntity<List<WniosekEnt>> getAll() {
        log.info("GET /wniosek - pobieranie wszystkich wniosków");
        return ResponseEntity.ok(wniosekRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<WniosekEnt> create(@RequestBody WniosekCreateRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak danych wniosku w body");
        }

        if (req.uzytkownikId == null || req.typ == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pola uzytkownikId oraz typ są wymagane"
            );
        }

        UzytkownikEnt u = uzytkownikRepo.findById(req.uzytkownikId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie ma użytkownika id=" + req.uzytkownikId
                ));

        PojazdEnt p = null;

        if (req.typ == TypWniosku.WYREJESTROWANIE || req.typ == TypWniosku.ZBYCIE) {
            if (req.pojazdId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Dla tego typu wniosku wymagane jest pojazdId"
                );
            }

            p = pojazdRepo.findById(req.pojazdId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Nie ma pojazdu id=" + req.pojazdId
                    ));
        }

        if (req.typ == TypWniosku.REJESTRACJA || req.typ == TypWniosku.CZASOWA) {
            walidujDaneNowegoPojazdu(req);
        }

        UrzednikEnt urz = null;

        if (req.urzednikId != null) {
            urz = urzednikRepo.findById(req.urzednikId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Nie ma urzędnika id=" + req.urzednikId
                    ));
        }

        if (!Boolean.TRUE.equals(req.oplacono)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wniosek może zostać wysłany tylko po oznaczeniu opłaty");
        }

        WniosekEnt w = new WniosekEnt();

        w.setTyp(req.typ);
        w.setOpis(req.opis);
        w.setUzytkownik(u);
        w.setPojazd(p);
        w.setUrzednik(urz);

        w.setVin(req.vin != null ? req.vin.trim().toUpperCase() : null);
        w.setMarka(req.marka);
        w.setModel(req.model);
        w.setRok(req.rok);
        w.setNumerRejestracyjny(req.numerRejestracyjny);
        w.setRodzajPojazdu(req.rodzajPojazdu);
        w.setPrzeznaczenie(req.przeznaczenie);
        w.setDataNabycia(req.dataNabycia);
        w.setTypTablic(req.typTablic);
        w.setZachowajNumer(req.zachowajNumer);
        w.setNumerIndywidualny(req.numerIndywidualny);
        w.setKwotaOplaty(req.kwotaOplaty != null ? req.kwotaOplaty : 0);

        w.setOplacono(Boolean.TRUE.equals(req.oplacono));

        WniosekEnt saved = wniosekRepo.save(w);

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/popraw")
    public ResponseEntity<WniosekEnt> poprawWniosek(
            @PathVariable Long id,
            @RequestBody WniosekCreateRequest req
    ) {
        WniosekEnt w = wniosekRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono wniosku id=" + id
                ));

        if (w.getStatus() != StatusWniosku.DO_POPRAWY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poprawiać można tylko wniosek ze statusem DO_POPRAWY"
            );
        }

        w.setOpis(req.opis);

        w.setVin(req.vin != null ? req.vin.trim().toUpperCase() : null);
        w.setMarka(req.marka);
        w.setModel(req.model);
        w.setRok(req.rok);
        w.setNumerRejestracyjny(req.numerRejestracyjny);
        w.setRodzajPojazdu(req.rodzajPojazdu);
        w.setPrzeznaczenie(req.przeznaczenie);
        w.setDataNabycia(req.dataNabycia);
        w.setTypTablic(req.typTablic);
        w.setZachowajNumer(req.zachowajNumer);
        w.setNumerIndywidualny(req.numerIndywidualny);

        w.setStatus(StatusWniosku.ZLOZONY);

        WniosekEnt saved = wniosekRepo.save(w);

        ws.convertAndSend(
                "/topic/wniosek/events",
                new WniosekStatusEvent(
                        id,
                        StatusWniosku.DO_POPRAWY,
                        StatusWniosku.ZLOZONY
                )
        );

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WniosekEnt> changeStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "komentarz", required = false) String komentarz
    ) {
        log.info("PATCH /wniosek/{}/status - status={}", id, status);

        WniosekEnt w = wniosekRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie ma wniosku id=" + id
                ));

        StatusWniosku oldStatus = w.getStatus();

        if (oldStatus == StatusWniosku.ZATWIERDZONY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można zmieniać statusu zatwierdzonego wniosku"
            );
        }

        StatusWniosku newStatus;

        try {
            newStatus = StatusWniosku.valueOf(status);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Niepoprawny status: " + status
            );
        }

        if (oldStatus == newStatus) {
            log.info("PATCH /wniosek/{}/status - status bez zmian ({})", id, newStatus);
            return ResponseEntity.ok(w);
        }

        if (newStatus == StatusWniosku.ZATWIERDZONY
                && !Boolean.TRUE.equals(w.getOplacono())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można zatwierdzić wniosku — opłata nie została oznaczona jako opłacona"
            );
        }

        try {
            if (komentarz != null) {
                w.setKomentarzUrzednika(komentarz);
            }

            if (newStatus == StatusWniosku.ZATWIERDZONY
                    && w.getPojazd() == null
                    && (w.getTyp() == TypWniosku.REJESTRACJA
                    || w.getTyp() == TypWniosku.CZASOWA)) {

                walidujDaneWnioskuPrzedUtworzeniemPojazdu(w);

                if (pojazdRepo.existsByVin(w.getVin())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Pojazd o takim VIN już istnieje"
                    );
                }

                PojazdEnt pojazd = new PojazdEnt();

                pojazd.setVin(w.getVin().trim().toUpperCase());
                pojazd.setMarka(w.getMarka());
                pojazd.setModel(w.getModel());
                pojazd.setRok(w.getRok());
                pojazd.setNumerRejestracyjny(wyznaczNumerRejestracyjny(w));
                pojazd.setUzytkownik(w.getUzytkownik());

                PojazdEnt zapisanyPojazd = pojazdRepo.save(pojazd);
                w.setPojazd(zapisanyPojazd);

                log.info(
                        "Utworzono pojazd id={} dla wniosku id={}",
                        zapisanyPojazd.getId(),
                        w.getId()
                );
            }

            if (newStatus == StatusWniosku.ZATWIERDZONY
                    && (w.getTyp() == TypWniosku.WYREJESTROWANIE
                    || w.getTyp() == TypWniosku.ZBYCIE)) {

                if (w.getPojazd() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Brak pojazdu do usunięcia"
                    );
                }

                Long pojazdId = w.getPojazd().getId();

                w.setPojazd(null);
                w.setKomentarzUrzednika(komentarz);
                w.setStatus(newStatus);

                WniosekEnt saved = wniosekRepo.save(w);

                pojazdRepo.deleteById(pojazdId);

                log.info(
                        "Usunięto pojazd id={} dla wniosku id={}",
                        pojazdId,
                        w.getId()
                );

                ws.convertAndSend(
                        "/topic/wniosek/events",
                        new WniosekStatusEvent(id, oldStatus, newStatus)
                );

                return ResponseEntity.ok(saved);
            }

            w.setKomentarzUrzednika(komentarz);
            w.setStatus(newStatus);

            WniosekEnt saved = wniosekRepo.save(w);

            log.info(
                    "Zmieniono status wniosku id={} z {} na {}",
                    id,
                    oldStatus,
                    newStatus
            );

            ws.convertAndSend(
                    "/topic/wniosek/events",
                    new WniosekStatusEvent(id, oldStatus, newStatus)
            );

            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException e) {
            log.error(
                    "PATCH /wniosek/{}/status - naruszenie integralności danych: {}",
                    id,
                    e.getMessage(),
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nie można zmienić statusu wniosku: konflikt danych lub naruszenie ograniczeń bazy.",
                    e
            );

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "PATCH /wniosek/{}/status - nieoczekiwany błąd: {}",
                    id,
                    e.getMessage(),
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd serwera podczas zmiany statusu",
                    e
            );
        }
    }

    @GetMapping("/uzytkownik/{id}")
    public ResponseEntity<List<WniosekEnt>> getByUzytkownik(@PathVariable Long id) {
        log.info("GET /wniosek/uzytkownik/{} - pobieranie wniosków użytkownika", id);
        return ResponseEntity.ok(wniosekRepo.findByUzytkownikId(id));
    }

    @GetMapping("/status/zlozony")
    public ResponseEntity<List<WniosekEnt>> getZlozone() {
        log.info("GET /wniosek/status/zlozony - pobieranie wniosków o statusie ZLOZONY");
        return ResponseEntity.ok(wniosekRepo.findZlozoneNative());
    }

    private void walidujDaneNowegoPojazdu(WniosekCreateRequest req) {
        if (req.vin == null || req.vin.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VIN jest wymagany");
        }

        if (req.vin.trim().length() != 17) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VIN musi mieć dokładnie 17 znaków"
            );
        }

        if (req.marka == null || req.marka.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marka jest wymagana");
        }

        if (req.model == null || req.model.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model jest wymagany");
        }

        int aktualnyRok = Year.now().getValue();

        if (req.rok == null || req.rok < 1900 || req.rok > aktualnyRok + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Niepoprawny rok produkcji"
            );
        }

        if (Boolean.TRUE.equals(req.zachowajNumer)
                && (req.numerRejestracyjny == null || req.numerRejestracyjny.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Zaznaczono zachowanie numeru, ale nie podano dotychczasowego numeru"
            );
        }

        if ("INDYWIDUALNE".equals(req.typTablic)
                && (req.numerIndywidualny == null || req.numerIndywidualny.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dla tablic indywidualnych wymagany jest własny numer rejestracyjny"
            );
        }
    }

    private void walidujDaneWnioskuPrzedUtworzeniemPojazdu(WniosekEnt w) {
        if (w.getVin() == null || w.getVin().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można utworzyć pojazdu: brak VIN"
            );
        }

        if (w.getVin().trim().length() != 17) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można utworzyć pojazdu: VIN musi mieć dokładnie 17 znaków"
            );
        }

        if (w.getMarka() == null || w.getMarka().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można utworzyć pojazdu: brak marki"
            );
        }

        if (w.getModel() == null || w.getModel().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można utworzyć pojazdu: brak modelu"
            );
        }

        int aktualnyRok = Year.now().getValue();

        if (w.getRok() == null || w.getRok() < 1900 || w.getRok() > aktualnyRok + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie można utworzyć pojazdu: niepoprawny rok produkcji"
            );
        }
    }

    private String wyznaczNumerRejestracyjny(WniosekEnt w) {
        if ("INDYWIDUALNE".equals(w.getTypTablic())) {
            if (w.getNumerIndywidualny() == null || w.getNumerIndywidualny().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Dla tablic indywidualnych wymagany jest własny numer rejestracyjny"
                );
            }

            return w.getNumerIndywidualny().trim().toUpperCase();
        }

        if (Boolean.TRUE.equals(w.getZachowajNumer())) {
            if (w.getNumerRejestracyjny() == null || w.getNumerRejestracyjny().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Zaznaczono zachowanie numeru, ale nie podano dotychczasowego numeru"
                );
            }

            return w.getNumerRejestracyjny().trim().toUpperCase();
        }

        return generujNumerRejestracyjny();
    }

    private String generujNumerRejestracyjny() {
        String litery = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String cyfry = "0123456789";

        StringBuilder sb = new StringBuilder("WX");

        for (int i = 0; i < 2; i++) {
            sb.append(litery.charAt((int) (Math.random() * litery.length())));
        }

        for (int i = 0; i < 3; i++) {
            sb.append(cyfry.charAt((int) (Math.random() * cyfry.length())));
        }

        return sb.toString();
    }
}