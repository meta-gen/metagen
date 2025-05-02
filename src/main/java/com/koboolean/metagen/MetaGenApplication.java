package com.koboolean.metagen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@BatchDataSource
@EnableJpaAuditing
public class MetaGenApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaGenApplication.class, args);
    }

}
