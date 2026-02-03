package pl.projekt.projekt.controllers;

import org.springframework.web.bind.annotation.*;
import pl.projekt.projekt.entity.UzytkownikEnt;
import pl.projekt.projekt.repo.UzytkownikRepo;

import java.util.List;

@RestController
@RequestMapping("/uzytkownik")
@CrossOrigin
public class UzytkownikController {

    private final UzytkownikRepo repo;

    public UzytkownikController(UzytkownikRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<UzytkownikEnt> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public UzytkownikEnt create(@RequestBody UzytkownikEnt uzytkownik) {
        return repo.save(uzytkownik);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
