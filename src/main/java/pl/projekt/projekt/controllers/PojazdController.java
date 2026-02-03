package pl.projekt.projekt.controllers;

import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.entity.PojazdEnt;
import pl.projekt.projekt.repo.PojazdRepo;

import java.util.List;

@RestController
@RequestMapping("/pojazd")
@CrossOrigin
public class PojazdController {

    private final PojazdRepo pojazdRepo;

    public PojazdController(PojazdRepo pojazdRepo) {
        this.pojazdRepo = pojazdRepo;
    }

    // GET /pojazd
    @GetMapping
    public List<PojazdEnt> getAll() {
        return pojazdRepo.findAll();
    }

    // POST /pojazd
    @PostMapping
    public PojazdEnt create(@RequestBody PojazdEnt pojazd) {
        return pojazdRepo.save(pojazd);
    }

    // DELETE /pojazd/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        pojazdRepo.deleteById(id);
    }
}
