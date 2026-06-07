package pl.projekt.projekt.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.PojazdEnt;

public interface PojazdRepo extends JpaRepository<PojazdEnt, Long> {

    boolean existsByVin(String vin);

    List<PojazdEnt> findByUzytkownikId(Long id);

}
