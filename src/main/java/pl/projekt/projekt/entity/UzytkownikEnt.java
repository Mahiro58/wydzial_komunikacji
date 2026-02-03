package pl.projekt.projekt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "uzytkownik")
@Data
public class UzytkownikEnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String imie;

    @Column(nullable = false, length = 80)
    private String nazwisko;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String telefon;

}
