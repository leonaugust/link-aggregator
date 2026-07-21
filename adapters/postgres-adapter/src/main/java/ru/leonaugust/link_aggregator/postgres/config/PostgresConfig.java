package ru.leonaugust.link_aggregator.postgres.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@ComponentScan(basePackages = "ru.leonaugust.link_aggregator.postgres")
@EnableJpaRepositories(basePackages = "ru.leonaugust.link_aggregator.postgres")
@EntityScan(basePackages = "ru.leonaugust.link_aggregator.postgres")
public class PostgresConfig {

}
