package com.example.demo.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Profile("!test")
@Configuration
public class R2dbcInitConfig {
    @Bean
    ConnectionFactoryInitializer r2dbcInitializer(ConnectionFactory cf) {
        var populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        var init = new ConnectionFactoryInitializer();
        init.setConnectionFactory(cf);
        init.setDatabasePopulator(populator);
        return init;
    }
}
