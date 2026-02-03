package pl.projekt.projekt.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "wniosek")
@Data
public class WniosekEnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypWniosku typ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusWniosku status = StatusWniosku.ZLOZONY;

    private String opis;

    @Column(name = "data_zlozenia", nullable = false)
    private LocalDateTime dataZlozenia;

    // relacje (Dużo wniosków do jednego użytkownika)
    @ManyToOne(optional = false)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private UzytkownikEnt uzytkownik;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pojazd_id", nullable = false)
    private PojazdEnt pojazd;

    @ManyToOne(optional = true)
    @JoinColumn(name = "urzednik_id")
    private UrzednikEnt urzednik;

    @PrePersist
    public void prePersist() {
        if (dataZlozenia == null) {
            dataZlozenia = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusWniosku.ZLOZONY;
        }
    }
}
