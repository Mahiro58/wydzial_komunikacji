package pl.projekt.projekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.projekt.projekt.controllers.dto.LoginRequest;
import pl.projekt.projekt.controllers.dto.RegisterRequest;
import pl.projekt.projekt.entity.Rola;
import pl.projekt.projekt.entity.UzytkownikEnt;
import pl.projekt.projekt.repo.UzytkownikRepo;
import pl.projekt.projekt.controllers.dto.AuthResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UzytkownikRepo repo;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        if (repo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Użytkownik z takim emailem już istnieje");
        }

        UzytkownikEnt user = new UzytkownikEnt();

        user.setImie(request.getImie());
        user.setNazwisko(request.getNazwisko());
        user.setEmail(request.getEmail());
        user.setTelefon(request.getTelefon());

        // Hashowanie hasła
        user.setHaslo(
                passwordEncoder.encode(request.getHaslo())
        );

        // Domyślna rola
        user.setRola(Rola.USER);

        repo.save(user);

        return "Rejestracja udana";
    }

    public AuthResponse login(LoginRequest request) {

    UzytkownikEnt user = repo.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new RuntimeException("Nie znaleziono użytkownika"));

    boolean matches = passwordEncoder.matches(
            request.getHaslo(),
            user.getHaslo()
    );

    if (!matches) {
        throw new RuntimeException("Błędne hasło");
    }

    return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getRola().name(),
            "Zalogowano pomyślnie"
    );
}
}