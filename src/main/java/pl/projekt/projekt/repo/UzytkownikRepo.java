package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.UzytkownikEnt;

import java.util.Optional;

public interface UzytkownikRepo extends JpaRepository<UzytkownikEnt, Long> {

    Optional<UzytkownikEnt> findByEmail(String email);
}