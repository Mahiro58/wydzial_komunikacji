package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.WniosekEnt;

public interface WniosekRepo extends JpaRepository<WniosekEnt, Long> {
}
