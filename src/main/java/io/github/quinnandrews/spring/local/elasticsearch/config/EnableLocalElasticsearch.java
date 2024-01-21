package io.github.quinnandrews.spring.local.elasticsearch.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Imports the necessary configuration.
 *
 * @author Quinn Andrews
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ElasticsearchContainerConfig.class)
public @interface EnableLocalElasticsearch {
}
