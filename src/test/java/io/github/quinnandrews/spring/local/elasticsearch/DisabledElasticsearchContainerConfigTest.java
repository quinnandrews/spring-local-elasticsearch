package io.github.quinnandrews.spring.local.elasticsearch;

import io.github.quinnandrews.spring.local.elasticsearch.application.Application;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ActiveProfiles("disabled")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = Application.class)
public class DisabledElasticsearchContainerConfigTest {

    @Autowired(required = false)
    private ElasticsearchContainer elasticsearchContainer;

    @Autowired(required = false)
    private ClientConfiguration clientConfiguration;

    @Autowired(required = false)
    private RestClient restClient;

    @Test
    @Order(1)
    void container_notInitialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // then the container is not initialized
        assertNull(elasticsearchContainer);
    }

    @Test
    @Order(2)
    void clientConfiguration_notInitialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // and the container is not initialized
        // then the clientConfiguration is not initialized
        assertNull(clientConfiguration);
    }

    @Test
    @Order(3)
    void restClient_notInitialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // and the container is not initialized
        // and the clientConfiguration is not initialized
        // then the restClient is still initialized
        assertNotNull(restClient);
        assertTrue(restClient.isRunning());
        // but it is initialized with the default URI
        assertEquals(
                "localhost:9200",
                restClient.getNodes().get(0).getHost().toHostString()
        );
    }
}
