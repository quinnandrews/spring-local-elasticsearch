package io.github.quinnandrews.spring.local.elasticsearch.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * <p> Initializes and configures a module from Testcontainers that runs
 * Elasticsearch inside a Docker Container. Requires minimal configuration
 * using Spring conventions, but a variety of optional properties are
 * supported to override default behavior.
 *
 * <p> See the project README for configuration details.
 *
 * @author Quinn Andrews
 */
@ConditionalOnProperty(name="spring.local.elasticsearch.enabled",
                       havingValue="true",
                       matchIfMissing = true)
@Configuration
public class ElasticsearchContainerConfig {

    public static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.7.1"; //7.9.2
    public static final Integer ELASTICSEARCH_DEFAULT_PORT = 9200;
    public static final String ELASTICSEARCH_DEFAULT_USERNAME = "elastic";
    public static final String ELASTICSEARCH_PASSWORD_ENV_KEY = "ELASTIC_PASSWORD";

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchContainerConfig.class);

    private final String containerImage;
    private final Integer containerPort;
    private final Boolean followContainerLog;
    private final String password;

    @Autowired
    public ElasticsearchContainerConfig(@Value("${spring.local.elasticsearch.container.image:#{null}}")
                                        final String containerImage,
                                        @Value("${spring.local.elasticsearch.container.port:#{null}}")
                                        final Integer containerPort,
                                        @Value("${spring.local.elasticsearch.container.log.follow:#{false}}")
                                        final Boolean followContainerLog,
                                        @Value("${spring.local.elasticsearch.password:#{null}}")
                                        final String password) {
        this.containerImage = containerImage;
        this.containerPort = containerPort;
        this.followContainerLog = followContainerLog;
        this.password = password;
    }

    /**
     * Initializes a Testcontainers Bean that runs ElasticsearchL inside a Docker Container
     * with the given configuration.
     *
     * @return ElasticsearchContainer
     */
    @Bean
    public ElasticsearchContainer elasticsearchContainer() {
        final var container = new ElasticsearchContainer(
                DockerImageName.parse(Optional.ofNullable(containerImage)
                        .orElse(ELASTICSEARCH_DEFAULT_IMAGE))
        );
        Optional.ofNullable(containerPort).ifPresent(cp ->
                container.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                        new HostConfig().withPortBindings(
                                new PortBinding(Ports.Binding.bindPort(cp),
                                        new ExposedPort(ELASTICSEARCH_DEFAULT_PORT)),
                                new PortBinding(Ports.Binding.empty(),
                                        new ExposedPort(9300))
                                )
                ))
        );
        Optional.ofNullable(password).ifPresent(container::withPassword);
        if (followContainerLog) {
            container.withLogConsumer(new Slf4jLogConsumer(logger));
        }
        container.start();
        logger.info(MessageFormat.format("""
                      
                      
                        *************************************************************************************
                        |+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|
                        
                            Running ElasticsearchContainer for development and testing.
                        
                            Built with Docker Image: {0}
                            Host Address URL: {1}
                            Username: {2}
                            Password: {3}

                            Note: The port referenced in the Host Address URL is a port to
                            access the container. Inside the container Elasticsearch is on
                            port 9200 as usual.
                                  
                        |+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|
                        *************************************************************************************
                        """,
                container.getDockerImageName(),
                container.getHttpHostAddress(),
                ELASTICSEARCH_DEFAULT_USERNAME,
                container.getEnvMap().get(ELASTICSEARCH_PASSWORD_ENV_KEY)));
        return container;
    }

    @ConditionalOnProperty(name="spring.local.elasticsearch.enabled",
                           havingValue="true",
                           matchIfMissing = true)
    @Configuration
    public static class ElasticsearchClientConfig extends ElasticsearchConfiguration {

        private final ElasticsearchContainer elasticsearchContainer;

        public ElasticsearchClientConfig(final ElasticsearchContainer elasticsearchContainer) {
            this.elasticsearchContainer = elasticsearchContainer;
        }

        /**
         * Initializes a Spring Bean that instructs how the Elasticsearch RestClient should
         * be configured.
         *
         * @return ClientConfiguration
         */
        @Override
        public @NotNull ClientConfiguration clientConfiguration() {
            final var credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            ELASTICSEARCH_DEFAULT_USERNAME,
                            elasticsearchContainer.getEnvMap().get(ELASTICSEARCH_PASSWORD_ENV_KEY)
                    )
            );
            return ClientConfiguration.builder()
                    .connectedTo(elasticsearchContainer.getHttpHostAddress())
                    .usingSsl()
                    .withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback.from(restClientBuilder -> {
                        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                        .setSSLContext(elasticsearchContainer.createSslContextFromCa())
                        );
                        return restClientBuilder;
                    }))
                    .build();
        }
    }
}
