package io.github.quinnandrews.spring.local.elasticsearch;

import io.github.quinnandrews.spring.local.elasticsearch.application.Application;
import io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.GuitarPedalDocument;
import io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.repository.GuitarPedalDocumentRepository;
import io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.repository.GuitarPedalRepository;
import io.github.quinnandrews.spring.local.elasticsearch.config.ElasticsearchContainerConfig;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ActiveProfiles("default")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = Application.class)
public class DefaultElasticsearchContainerConfigTest {

    @Autowired(required = false)
    private ElasticsearchContainer elasticsearchContainer;

    @Autowired(required = false)
    private ClientConfiguration clientConfiguration;

    @Autowired(required = false)
    private RestClient restClient;

    @Autowired(required = false)
    private GuitarPedalRepository guitarPedalRepository;

    @Autowired(required = false)
    private GuitarPedalDocumentRepository guitarPedalDocumentRepository;

    @Test
    @Order(1)
    void container_initialized() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        assertNotNull(elasticsearchContainer);
        assertTrue(elasticsearchContainer.isRunning());
        // then the container matches the default configuration
        assertEquals(ElasticsearchContainerConfig.ELASTICSEARCH_DEFAULT_IMAGE, elasticsearchContainer.getDockerImageName());
        assertNotNull(elasticsearchContainer.getMappedPort(ElasticsearchContainerConfig.ELASTICSEARCH_DEFAULT_PORT));
        assertEquals(
                ElasticsearchContainer.ELASTICSEARCH_DEFAULT_PASSWORD,
                elasticsearchContainer.getEnvMap().get(ElasticsearchContainerConfig.ELASTICSEARCH_PASSWORD_ENV_KEY)
        );
    }

    @Test
    @Order(2)
    void clientConfiguration_initialized() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        // then the clientConfiguration is initialized
        assertNotNull(clientConfiguration);
    }

    @Test
    @Order(3)
    void restClient_initialized() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        // and the clientConfiguration is initialized
        // then the restClient is initialized
        assertNotNull(restClient);
        assertTrue(restClient.isRunning());
        // and the restClient matches the container
        assertEquals(
                elasticsearchContainer.getHttpHostAddress(),
                restClient.getNodes().get(0).getHost().toHostString()
        );
    }

    @Test
    @Order(4)
    void guitarPedalDocumentRepository_initialized_dataWriteableAndReadable() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        // and the clientConfiguration is initialized
        // and the restClient is initialized
        // and the guitarPedalDocumentRepository is initialized
        assertNotNull(guitarPedalDocumentRepository);
        // and the guitarPedalRepository is initialized
        assertNotNull(guitarPedalRepository);
        // and the database contains three pedals
        assertEquals(3, guitarPedalRepository.count());
        // but elasticsearch contains no pedals
        assertEquals(0, guitarPedalDocumentRepository.count());
        // when the pedals in the database are added to elasticsearch
        guitarPedalDocumentRepository.saveAll(
                guitarPedalRepository.findAll().stream()
                        .map(GuitarPedalDocument::new)
                        .toList());
        // then elasticsearch contains three pedals
        assertEquals(3, guitarPedalDocumentRepository.count());
    }

    @Test
    @Order(5)
    void indexSearchable_findAllPedals() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        // and the clientConfiguration is initialized
        // and the restClient is initialized
        // and the guitarPedalDocumentRepository is initialized
        // and elasticsearch contains three pedals
        // when the pedals are searched
        // and sorted by name
        final var pedals = StreamSupport.stream(
                guitarPedalDocumentRepository.findAll(Sort.by("name")).spliterator(),
                Boolean.FALSE
        ).toList();
        // then matching pedals are returned sorted by name
        assertEquals(3L, pedals.get(0).getId());
        assertEquals("Catalinbread Soft Focus Reverb", pedals.get(0).getName());
        assertEquals(1L, pedals.get(1).getId());
        assertEquals("Electro-Harmonix Big Muff Fuzz", pedals.get(1).getName());
        assertEquals(2L, pedals.get(2).getId());
        assertEquals("Strymon Deco: Tape Saturation and Double Tracker", pedals.get(2).getName());
    }

    @Test
    @Order(6)
    void indexReadable_getPedalById() {
        // given the application is initialized
        // and the 'default' profile is active
        // and the container is initialized
        // and the clientConfiguration is initialized
        // and the restClient is initialized
        // and the guitarPedalDocumentRepository is initialized
        // and elasticsearch contains three pedals
        // when the pedal is queried by id
        final var optionalPedal = guitarPedalDocumentRepository.findById(3L);
        // then the pedal associated with that id is returned
        assertTrue(optionalPedal.isPresent());
        assertEquals(3L, optionalPedal.get().getId());
        assertEquals("Catalinbread Soft Focus Reverb", optionalPedal.get().getName());
    }
}
