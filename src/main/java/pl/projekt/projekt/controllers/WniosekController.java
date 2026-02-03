package pl.projekt.projekt.controllers;

import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.controllers.dto.WniosekCreateRequest;
import pl.projekt.projekt.entity.*;
import pl.projekt.projekt.repo.*;

import java.util.List;

@RestController
@RequestMapping("/wniosek")
@CrossOrigin
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

    @GetMapping
    public List<WniosekEnt> getAll() {
        return wniosekRepo.findAll();
    }

    @PostMapping
    public WniosekEnt create(@RequestBody WniosekCreateRequest req) {

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
}
