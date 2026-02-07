package pl.projekt.projekt.external;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.projekt.projekt.external.dto.VpicDecodeResponse;
import reactor.core.publisher.Mono;

@Service
public class VpicClient {

    private static final Logger log = LogManager.getLogger(VpicClient.class);

    private static String SERVER;
    private static String API_PATH_FMT;
    private static WebClient wc;

    @Autowired
    public void loadServiceConfig(
            @Value("${_service.name}") String name,
            @Value("${_service.port}") String port,
            @Value("${_service.api_path}") String ap,
            WebClient.Builder wcBuilder
    ) {
        SERVER = "https://" + name; // port 443 => https
        API_PATH_FMT = ap;
        wc = wcBuilder.baseUrl(SERVER).build();
        log.info("VpicClient skonfigurowany: baseUrl={}, apiPathFmt={}", SERVER, API_PATH_FMT);
    }

    @Transactional
    public VpicDecodeResponse decodeVinValues(String vin) {
        try {
            String path = String.format(API_PATH_FMT, vin);
            log.info("Wywołuję vPIC: GET {}{}", SERVER, path);

            Mono<VpicDecodeResponse> mono = wc.get()
                    .uri(path)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(VpicDecodeResponse.class);

            return mono.block();

        } catch (WebClientResponseException e) {
            log.error("vPIC błąd HTTP: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("vPIC błąd wywołania: {}", e.getMessage(), e);
            return null;
        }
    }
}
