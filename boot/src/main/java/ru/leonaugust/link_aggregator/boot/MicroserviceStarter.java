package ru.leonaugust.link_aggregator.boot;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import ru.leonaugust.link_aggregator.postgres.config.PostgresConfig;

@Import(PostgresConfig.class)
@EnableAutoConfiguration
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "ru.leonaugust.link_aggregator")
public class MicroserviceStarter {

  public static void main(String[] args) {
    new SpringApplicationBuilder(MicroserviceStarter.class)
        .bannerMode(Mode.LOG)
        .run(args);
  }
}