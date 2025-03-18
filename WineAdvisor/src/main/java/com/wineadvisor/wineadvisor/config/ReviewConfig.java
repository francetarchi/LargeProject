package com.wineadvisor.wineadvisor.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
public class ReviewConfig {

    @Bean
    public MongoClient mongoClient() {
        
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                .build());
        
        return mongoClient;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // Uso Spring Security per configurare le autorizzazioni e il filtraggio delle richieste HTTP
        http
                // CSRF (Cross-Site Request Forgery) è un tipo di attacco che sfrutta la fiducia di un sito web nei confronti di un utente autenticato.
                // Disabilitare CSRF può essere utile in ambienti dove l'applicazione non gestisce sessioni di browser o dove si utilizzano API RESTful,
                // che di solito non hanno bisogno di protezione CSRF.
                // In questo caso, per semplificare la configurazione, il CSRF è disabilitato.
                .csrf(csrf -> csrf.disable())
                // Autorizzazione: Queste righe definiscono le regole per l'accesso alle varie risorse dell'applicazione.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/events/admin/**").hasRole("ADMIN") // Solo gli utenti con il ruolo ADMIN possono accedere a tutte le risorse sotto /api/events/admin/**. Quindi, qualsiasi richiesta a questa URL sarà consentita solo se l'utente ha il ruolo ADMIN.
                        .requestMatchers("/api/events/**").permitAll() // Le richieste a /api/events/** sono pubbliche, quindi chiunque può accedervi senza bisogno di autenticazione.
                        .anyRequest().authenticated() // Per tutte le altre richieste (che non corrispondono a quelle sopra specificate), è necessario che l'utente sia autenticato. Quindi, l'accesso è consentito solo agli utenti che hanno effettuato il login.
                )
                .httpBasic(withDefaults());
                // httpBasic: Questa parte configura l'autenticazione di tipo Basic Authentication. In questo tipo di autenticazione,
                // l'utente invia le proprie credenziali (username e password) come parte della richiesta HTTP (in genere nell'intestazione Authorization).
                // withDefaults(): È una configurazione predefinita per l'autenticazione HTTP di base, che consente di utilizzare un modulo di login molto semplice.
                // Questo tipo di autenticazione è utile per le API, ma NON è SICURO da utilizzare da solo in un'applicazione web con interfaccia utente.
        return http.build();
        // Dopo aver definito tutte le configurazioni di sicurezza, la riga qui sopra costruisce l'oggetto SecurityFilterChain e lo restituisce.
        // Questo oggetto è responsabile per l'applicazione delle regole di sicurezza definite precedentemente.
    }
}
