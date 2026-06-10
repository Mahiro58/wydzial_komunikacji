package pl.projekt.projekt.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.projekt.projekt.entity.DokumentEnt;
import pl.projekt.projekt.entity.WniosekEnt;
import pl.projekt.projekt.repo.DokumentRepo;
import pl.projekt.projekt.repo.WniosekRepo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/dokumenty")
@CrossOrigin
public class DokumentController {

    private final DokumentRepo dokumentRepo;
    private final WniosekRepo wniosekRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final Set<String> DOZWOLONE_TYPY = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    public DokumentController(
            DokumentRepo dokumentRepo,
            WniosekRepo wniosekRepo
    ) {
        this.dokumentRepo = dokumentRepo;
        this.wniosekRepo = wniosekRepo;
    }

    @PostMapping("/wniosek/{wniosekId}")
    public ResponseEntity<List<DokumentEnt>> uploadDokumentow(
            @PathVariable Long wniosekId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("typy") String[] typy
    ) {

        WniosekEnt wniosek = wniosekRepo.findById(wniosekId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono wniosku id=" + wniosekId
                ));

        if (files == null || files.length == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nie przesłano plików"
            );
        }

        if (typy == null || typy.length == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Brak typów dokumentów"
            );
        }

        try {
            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            for (int i = 0; i < files.length; i++) {

                MultipartFile file = files[i];

                if (file == null || file.isEmpty()) {
                    continue;
                }

                // rozmiar pliku
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Plik "
                                    + file.getOriginalFilename()
                                    + " przekracza limit 10 MB"
                    );
                }

                // typ MIME
                String contentType = file.getContentType();

                if (contentType == null
                        || !DOZWOLONE_TYPY.contains(contentType)) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dozwolone są tylko pliki PDF, JPG oraz PNG"
                    );
                }

                String typDokumentu =
                        i < typy.length
                                ? typy[i]
                                : "INNY";

                String originalName =
                        file.getOriginalFilename() != null
                                ? file.getOriginalFilename()
                                : "plik";

                // bezpieczna nazwa pliku
                String safeName = originalName
                        .replaceAll("[^a-zA-Z0-9._-]", "_");

                String storedName =
                        UUID.randomUUID() + "_" + safeName;

                Path target =
                        uploadPath.resolve(storedName);

                Files.copy(
                        file.getInputStream(),
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );

                DokumentEnt dokument =
                        new DokumentEnt();

                dokument.setWniosek(wniosek);
                dokument.setTypDokumentu(typDokumentu);
                dokument.setNazwaPliku(originalName);
                dokument.setSciezkaPliku(target.toString());
                dokument.setContentType(contentType);

                dokumentRepo.save(dokument);
            }

            return ResponseEntity.ok(
                    dokumentRepo.findByWniosekId(wniosekId)
            );

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd zapisu pliku",
                    e
            );
        }
    }

    @GetMapping("/wniosek/{wniosekId}")
    public ResponseEntity<List<DokumentEnt>> getDokumentyWniosku(
            @PathVariable Long wniosekId
    ) {
        return ResponseEntity.ok(
                dokumentRepo.findByWniosekId(wniosekId)
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> pobierzDokument(
            @PathVariable Long id
    ) {

        DokumentEnt dokument = dokumentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono dokumentu"
                ));

        try {
            Path path = Paths.get(
                    dokument.getSciezkaPliku()
            ).toAbsolutePath().normalize();

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Plik nie istnieje na dysku"
                );
            }

            String contentType =
                    dokument.getContentType() != null
                            ? dokument.getContentType()
                            : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + dokument.getNazwaPliku()
                                    + "\""
                    )
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd odczytu pliku",
                    e
            );
        }
    }
}