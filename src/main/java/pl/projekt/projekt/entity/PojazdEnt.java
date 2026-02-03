package pl.projekt.projekt.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pojazd")
@Data
public class PojazdEnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "vin", nullable = false, length = 17, unique = true)
    private String vin;

    @Column(name = "marka", nullable = false, length = 60)
    private String marka;

    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @Column(name = "rok")
    private Short rok;

    @Column(name = "numer_rejestracyjny", length = 12)
    private String numerRejestracyjny;
}
