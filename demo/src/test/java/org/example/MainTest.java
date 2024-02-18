package org.example;

import io.specmesh.avro.random.generator.API;
import io.specmesh.blackbox.testharness.kafka.DockerKafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.KafkaEnvironment;
import io.specmesh.cli.Provision;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class MainTest {

    private static final String OWNER_USER = "admin";
    @RegisterExtension
    private static final KafkaEnvironment KAFKA_ENV =
            DockerKafkaEnvironment.builder()
                    .withSaslAuthentication(
                            "admin", "admin-secret", OWNER_USER, OWNER_USER + "-secret")
                    .withKafkaAcls()
                    .build();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void doThings() throws InterruptedException, ExecutionException, TimeoutException {

        Admin adminClient = KAFKA_ENV.adminClient();
        Collection<TopicListing> listings = adminClient.listTopics().listings().get(10, TimeUnit.SECONDS);

        System.out.println(listings);


        final var provision = Provision.builder()
                .brokerUrl(KAFKA_ENV.kafkaBootstrapServers())
                .username(OWNER_USER)
                .secret("admin-secret")
                .spec("blackbox/simple_schema_demo-api.yml")
                .schemaPath("blackbox")
                .schemaRegistryUrl(KAFKA_ENV.schemeRegistryServer())
                .build();
        try {
            provision.call();

            final var status = provision.state();
            assertThat(status.topics(), hasSize(1));


            Files.list(Paths.get("resources/blackbox/shouldProcessEvents_1"))
                    .filter(path -> path.getFileName().endsWith(".avro"))
                    .forEach(consumer -> {
                        new API(10, "id", consumer.getFileName().toFile().getPath())
                                .run((key, record) -> {
                                    System.out.println("send to Kafka");
                                });
                    });

            // generate data
            String schema = "";
            final var api = new API(10, "id",
                    "blackbox/shouldProcessEvents_1/tube-passengers.avro");



        } catch (Exception e) {
            System.out.println("Boooom");
            e.printStackTrace();
            Thread.sleep(1000 * 100000);
            throw new RuntimeException(e);
        }

        int result = new Main().doThings();
        assertThat(result, is(1));

    }
}