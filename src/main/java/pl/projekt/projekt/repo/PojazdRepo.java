package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.PojazdEnt;

public interface PojazdRepo extends JpaRepository<PojazdEnt, Long> {

    boolean existsByVin(String vin);

}
