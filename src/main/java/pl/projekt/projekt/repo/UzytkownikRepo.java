package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.UzytkownikEnt;

public interface UzytkownikRepo extends JpaRepository<UzytkownikEnt, Long> {
}
