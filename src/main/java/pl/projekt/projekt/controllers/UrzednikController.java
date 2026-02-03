package pl.projekt.projekt.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.projekt.projekt.entity.UrzednikEnt;
import pl.projekt.projekt.repo.UrzednikRepo;

@RestController
@RequestMapping("/urzednik")
@CrossOrigin
public class UrzednikController {

    private final UrzednikRepo repo;

    public UrzednikController(UrzednikRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<UrzednikEnt> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public UrzednikEnt create(@RequestBody UrzednikEnt urzednik) {
        return repo.save(urzednik);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
