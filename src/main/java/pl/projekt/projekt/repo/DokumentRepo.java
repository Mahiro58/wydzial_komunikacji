package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.projekt.entity.DokumentEnt;

import java.util.List;

public interface DokumentRepo extends JpaRepository<DokumentEnt, Long> {

    List<DokumentEnt> findByWniosekId(Long wniosekId);
}