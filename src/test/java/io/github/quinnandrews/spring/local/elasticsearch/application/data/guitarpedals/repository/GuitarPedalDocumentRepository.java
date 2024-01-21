package io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.repository;

import io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.GuitarPedalDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/*
 This Bean is inactive with the "disabled" profile so that it doesn't block
 the tests in DisabledElasticsearchContainerConfigTests with an Exception
 that prevents the Application Context from starting up.
 */
@Profile({"custom", "default"})
@Repository
public interface GuitarPedalDocumentRepository extends ElasticsearchRepository<GuitarPedalDocument, Long> {
}
