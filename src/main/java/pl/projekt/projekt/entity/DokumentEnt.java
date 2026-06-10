package pl.projekt.projekt.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "dokument")
@Data
public class DokumentEnt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typDokumentu;

    private String nazwaPliku;

    private String sciezkaPliku;

    private String contentType;

    private LocalDateTime dataDodania;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wniosek_id", nullable = false)
    private WniosekEnt wniosek;

    @PrePersist
    public void prePersist() {
        if (dataDodania == null) {
            dataDodania = LocalDateTime.now();
        }
    }
}