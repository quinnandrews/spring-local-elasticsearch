# Spring Local Elasticsearch

## Description
Decorates the Testcontainers Elasticsearch module with enhanced configuration for Spring Boot Applications.

Requires minimal configuration using Spring conventions, but a variety of optional properties are provided to override default behavior by profile, supporting local development in addition to test execution.

## Features
- Configure whether the Testcontainers Elasticsearch module is active or not. Allows you to control its activation by profile.
- Configure the Docker Image to use with the Testcontainers Elasticsearch module. Allows you to match the Elasticsearch version used in local and test environments with the version in production.
- Configure the Testcontainers Elasticsearch module to run with a fixed container name. Useful for local development so that developers can easily find the running container.
- Configure the Testcontainers Elasticsearch module to run with a fixed port. Useful for local development so that developers can connect with consistent, predictable configuration.
- Configure whether to follow the Docker Container's log output. Useful for troubleshooting in some cases.

## Rationale
When developing an Application that uses Elasticsearch in production, an instance of Elasticsearch is needed for testing and local development, and it should be configurable in a way where it will spin up and tear down when the Application starts up and shuts down.

While Testcontainers is designed to provide that spin up/tear down capability to support Integration Tests, this project is designed to support running the Application locally as well, reducing the overhead that would come with maintaining Testcontainers in addition to some other solution that fundamentally does the same thing. There is no need to maintain a local Elasticsearch server nor additional Docker configuration inside or outside the project.

## Requirements
### Java 17
https://adoptium.net/temurin/releases/?version=17

### Docker
https://www.docker.com/products/docker-desktop/ <br>
https://rancherdesktop.io/

NOTE: Be sure to allocate at least 8GB of memory, otherwise the Elasticsearch Container will not start properly.

NOTE: Rancher Desktop may not work correctly if Docker Desktop had been previously installed.

## Elasticsearch 8 or higher
Versions less than version 7 are not supported. 

## Transitive Dependencies
- Spring Boot Starter Web 3.2.0
- Spring Boot Configuration Processor 3.2.0
- Spring Boot Starter Data Elasticsearch 3.2.0
- Spring Boot Testcontainers 3.2.0
- Testcontainers Elasticsearch 1.19.3

## Usage
### Configuration as a Test Dependency
While it is possible to declare `spring-local-elasticsearch` as a compile dependency, and control its activation with profiles, it is better practice to declare it as a test dependency.

This means, however, that all configuration for `spring-local-elasticsearch` (for both Integration Tests *and* for running the Application locally) can only reside in your project's test source. For Integration Tests this is common practice, but for running the Application locally it may seem unusual or perhaps difficult to do. However, by implementing the approach surfaced in this [article](https://bsideup.github.io/posts/local_development_with_testcontainers/) by Sergei Egorov, configuring a local profile in your project's test source becomes a simple process that will likely become a preferred practice as well.

### Add Spring Local Elasticsearch
Add the `spring-local-elasticsearch` artifact to your project as a test dependency:
```xml
<dependency>
    <groupId>io.github.quinnandrews</groupId>
    <artifactId>spring-local-elasticsearch</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```
(NOTE: The `spring-local-elasticsearch` artifact is NOT yet available in Maven Central, but is available from GitHub Packages, which requires additional configuration in your pom.xml file.)

### Configure a Local Profile
Create a properties files in your test resources directory to configuration for a `local` profile.

Configure the `local` profile with a fixed container name, so that developers can quickly and consistently identify the running container.

Configure the `local` profile with a fixed port, so that developers can connect with a consistent and predictable port.

Set other configuration properties as desired, or not at all to use default settings.

application-local.properties:
```properties
spring.local.elasticsearch.container.image=docker.elastic.co/elasticsearch/elasticsearch:8.10.2
spring.local.elasticsearch.container.name=local_elasticsearch
spring.local.elasticsearch.container.port=19200
spring.local.elasticsearch.password=flange_local
```

In the example above, in addition to a fixed container name and port, the `local` profile has the following settings:
- The `docker.elastic.co/elasticsearch/elasticsearch:8.10.2` Docker Image is set to use a more recent version of Elasticsearch than the default (`docker.elastic.co/elasticsearch/elasticsearch:8.7.1`) to match  production.
- The `flange_local` password is set to avoid using the default, and to isolate the local environment from the test environment.

### Implement a Spring Boot Application Class to Run the Application with the Local Profile
Add a Spring Boot Application Class named `LocalDevApplication` to your project's test source, preferably in the same package as the Spring Boot Application Class in the main source, to mirror the convention of Test Classes residing in the same package as the Classes they test.

Annotate `LocalDevApplication` with `@EnableLocalElasticsearch` and `@Profile("local")`. The `@EnableLocalElasticsearch` activates configuration of the Testcontainers Elasticsearch module while `@Profile("local")` ensures that configuration declared within the `LocalDevApplication` is only scanned and initialized if the `local` profile is active.

Inside the body of the `main` method, instantiate an instance of `SpringApplication` with the Application Class residing in the main source, to ensure that configuration in the main source is scanned. Then activate the `local` profile programmatically by calling `setAdditionalProfiles`. This will allow you to run `LocalDevApplication` in IntelliJ IDEA by simply right-clicking on the Class in the Project Panel and selecting `Run 'LocalDevApplication'` without having to add the `local` profile to the generated Spring Boot Run Configuration.

LocalDevApplication.java:
```java
@EnableLocalElasticsearch
@Profile("local")
@SpringBootApplication
public class LocalDevApplication {

    public static void main(final String[] args) {
        final var springApplication = new SpringApplication(Application.class);
        springApplication.setAdditionalProfiles("local");
        springApplication.run(args);
    }
}
```
### Configure a Test Profile

Configure the `test` profile to use a random container name and port by leaving their properties undeclared so that default settings will be used. Random names and ports are best practice for Integration Tests, and means that Integration Tests can be executed while the Application is running locally with the `local` profile.

Set other configuration properties as desired, or not at all to use default settings.

application-test.properties:
```properties
spring.local.elasticsearch.container.image=docker.elastic.co/elasticsearch/elasticsearch:8.10.2
spring.local.elasticsearch.password=flange_test
```
In the example above, the `test` profile has the following settings:
- The `docker.elastic.co/elasticsearch/elasticsearch:8.10.2` Docker Image is set to use a more recent version of Elasticsearch than the default (`docker.elastic.co/elasticsearch/elasticsearch:8.7.1`) to match production.
- The `flange_test` password is set to avoid using the default, and to isolate the test environment from the local environment.
- 
NOTE: One can, of course, configure the test profile and local profile to use the same passwords, but using distinct passwords is recommended, since the isolation will lower the risk of potential issues.

#### Implement an Integration Test

Add an Integration Test Class. Annotate with `@EnableLocalElasticsearch`, `@ActiveProfiles("test")` and `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`. The `@EnableLocalElasticsearch` activates configuration of the Testcontainers Elasticsearch module. The `@ActiveProfiles("test")` will activate the `test` profile when executed. And the `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`.

Write a test. The example below uses the RestAssured framework to call a REST endpoint backed by the Elasticsearch Container.

Example:

```java
@EnableLocalElasticsearch
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GuitarPedalControllerTest {

    @LocalServerPort
    private Integer port;

    @Test
    void getAllGuitarPedals()  {
        given().port(port)
                .when().get("/guitar-pedals")
                .then().statusCode(HttpStatus.OK.value())
                .and().contentType(ContentType.JSON)
                .and().body("size()", equalTo(4))
                .and().body("guitarPedalName", hasItems(
                        "Big Muff Fuzz",
                        "Deco: Tape Saturation and Double Tracker",
                        "Soft Focus Reverb",
                        "Sneak Attack: Attack/Decay and Tremolo"))
                .and().body("dateSold", hasItems(null, null, null, "2023-03-21"));
    }
}
```
NOTE: It is, of course, possible to declare `@EnableLocalElasticsearch` in a Spring Boot Application Class, named `TestApplication`, for example, so that one does not have to add `@EnableLocalElasticsearch` to every test Class, and that may be appropriate in some cases, but in general it is recommended that each test Class controls the declaration of the resources it needs. After all, some test Classes may need both Elasticsearch and Kafka, for instance, while other test Classes may only need one or the other. In such a case, initializing Elasticsearch and Kafka containers for all test Classes would waste resources and prolong the time it takes for test Classes to run.

## Supported Configuration Properties
**spring.local.elasticsearch.engaged**<br/>
Whether the containerized Elasticsearch server should be configured and started when the Application starts. By default, it is set to `true`. To disengage, set to `false`.

**spring.local.elasticsearch.container.image**<br/>
The Docker Image with the chosen version of Elasticsearch (example: `docker.elastic.co/elasticsearch/elasticsearch:8.10.2`). If undefined, a default will be used (`docker.elastic.co/elasticsearch/elasticsearch:8.7.1`).

**spring.local.elasticsearch.container.name**<br/>
The name to use for the Docker Container when started. If undefined, a random name is used. Random names are preferred for Integration Tests, but when running the Application locally, a fixed name is useful, since it allows developers to find the running container with a consistent, predictable name.

**spring.local.elasticsearch.container.port**<br/>
The port on the Docker Container to map with the Elasticsearch port inside the container. If undefined, a random port is used. Random ports are preferred for Integration Tests, but when running the Application locally, a fixed port is useful, since it allows developers to configure any connecting, external tools or apps with a consistent, predictable port.

**spring.local.elasticsearch.container.log.follow**<br/>
Whether the Application should log the output produced by the container's log. By default, container logs are not followed. Set with `true` to see their output.

**spring.local.elasticsearch.password**<br/>
The password the Application will use to connect with Elasticsearch. If undefined, Testcontainers will use its default (`changeme`). NOTE: The corresponding username is not configurable. It will be `elastic` in all cases.
