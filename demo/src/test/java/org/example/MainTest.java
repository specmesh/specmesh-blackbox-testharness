package org.example;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.specmesh.avro.random.generator.API;
import io.specmesh.blackbox.testharness.kafka.DockerKafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.KafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.clients.Clients;
import io.specmesh.cli.Provision;
import io.specmesh.kafka.provision.Status;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    void doThings() throws Exception {

        generateSeedData(provisionSeedData());


        int result = new Main().doThings();
        assertThat(result, is(1));

    }

    private static void generateSeedData(final Status status) throws IOException {
        final var topicName = status.topics().iterator().next().name();
        final var schema = status.schemas().iterator().next().getSchema();

        final var api = new API(100, "username", Files.readString(Paths.get(
                "build/resources/test/blackbox/shouldProcessEvents_1/tube-passengers.avro")));

        try (var producer = avroProducer("tube.passengers", "admin")) {
            api.run((key, genericRecord) -> {
                try {
                    final var recordMetadata = producer.send(
                                    new ProducerRecord<>(topicName,
                                            (String) key, cloneUsingOriginalSchema(genericRecord, schema)))
                            .get(5, TimeUnit.SECONDS);
                    System.out.println("Partition:" + recordMetadata.partition());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static Status provisionSeedData() throws Exception {
        final var provision = Provision.builder()
                .brokerUrl(KAFKA_ENV.kafkaBootstrapServers())
                .aclDisabled(false)
                .username(OWNER_USER)
                .secret(OWNER_USER + "-secret")
                .spec("build/resources/test/blackbox/seed/tube-passengers-api.yml")
                .schemaPath("build/resources/test/blackbox")
                .schemaRegistryUrl(KAFKA_ENV.schemeRegistryServer())
                .build();
        provision.call();

        final var status = provision.state();
        assertThat("Expected 1 topic", status.topics(), hasSize(1));
        assertThat("Topic failed to provision: " +status, status.topics().iterator().next().state(),
                is(Status.STATE.CREATED));
        return status;
    }

    private static GenericData.Record cloneUsingOriginalSchema(final GenericRecord genericRecord, final ParsedSchema schema) {
        final var record = new GenericData.Record((Schema) schema.rawSchema());
        final var fields = genericRecord.getSchema().getFields();
        fields.forEach(field -> record.put(field.name(), genericRecord.get(field.name())));
        return record;
    }

    private static Producer<String, GenericRecord> avroProducer(final String domainId, final String user) {

        return producer(domainId, GenericRecord.class, KafkaAvroSerializer.class, user,
                Map.of(
                // change for common data types
                // "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy",
                        "schema.reflection", "false"));
    }

    private static <V> Producer<String, V> producer(
            final String domainId,
            final Class<V> valueClass,
            final Class<?> valueSerializer,
            final String userName,
            final Map<String, Object> additionalProps) {
        final Map<String, Object> props =
                Clients.producerProperties(
                        domainId,
                        UUID.randomUUID().toString(),
                        KAFKA_ENV.kafkaBootstrapServers(),
                        KAFKA_ENV.schemeRegistryServer(),
                        StringSerializer.class,
                        valueSerializer,
                        false,
                        additionalProps);

        props.putAll(Clients.clientSaslAuthProperties(userName, userName + "-secret"));

        return Clients.producer(String.class, valueClass, props);
    }
}