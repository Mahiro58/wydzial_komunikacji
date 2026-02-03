package pl.projekt.projekt.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pl.projekt.projekt.entity.WniosekEnt;
import java.util.List;

public interface WniosekRepo extends JpaRepository<WniosekEnt, Long> {
    // JPQL (@Query)
    @Query("""
        SELECT w
        FROM WniosekEnt w
        WHERE w.uzytkownik.id = :uzytkownikId
    """)
    List<WniosekEnt> findByUzytkownikId(@Param("uzytkownikId") Long uzytkownikId);

    // NativeQuery (SQL)
    @Query(
        value = "SELECT * FROM wniosek WHERE status = 'ZLOZONY'",
        nativeQuery = true
    )
    List<WniosekEnt> findZlozoneNative();
}
