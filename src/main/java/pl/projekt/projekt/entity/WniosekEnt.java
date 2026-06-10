package pl.projekt.projekt.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
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

    private String vin;
    private String marka;
    private String model;
    private Short rok;
    private String numerRejestracyjny;
    private String rodzajPojazdu;
    private String przeznaczenie;
    private LocalDate dataNabycia;
    private String typTablic;
    private Boolean zachowajNumer;
    private String numerIndywidualny;
    private Integer kwotaOplaty = 0;
    private Boolean oplacono = false;

    @Column(name = "data_zlozenia", nullable = false)
    private LocalDateTime dataZlozenia;

    // relacje (Dużo wniosków do jednego użytkownika)
    @ManyToOne(optional = false)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private UzytkownikEnt uzytkownik;

    @ManyToOne(optional = true)
    @JoinColumn(name = "pojazd_id", nullable = true)
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
