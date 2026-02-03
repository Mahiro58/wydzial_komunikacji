package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.UrzednikEnt;

public interface UrzednikRepo extends JpaRepository<UrzednikEnt, Long> {
}