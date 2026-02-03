package pl.projekt.projekt.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class PersistenceJPAConfig {

    @Value("${baza.sterownik}")
    private String bazaSterownik;

    @Value("${baza.ip}")
    private String bazaIp;

    @Value("${baza.port}")
    private String bazaPort;

    @Value("${baza.nazwa}")
    private String bazaNazwa;

    @Value("${baza.protokol}")
    private String bazaProtokol;

    @Value("${baza.uzytkownik}")
    private String bazaUzytkownik;

    @Value("${baza.haslo}")
    private String bazaHaslo;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(bazaSterownik);
        ds.setUrl(bazaProtokol + "//" + bazaIp + ":" + bazaPort + "/" + bazaNazwa);
        ds.setUsername(bazaUzytkownik);
        ds.setPassword(bazaHaslo);
        return ds;
    }
}
