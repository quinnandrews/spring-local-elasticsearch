package io.github.quinnandrews.spring.local.elasticsearch.application;

import io.github.quinnandrews.spring.local.elasticsearch.config.EnableLocalElasticsearch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories
@EnableLocalElasticsearch
@SpringBootApplication
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
