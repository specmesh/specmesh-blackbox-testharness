/*
 * Copyright 2023 SpecMesh Contributors (https://github.com/specmesh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.specmesh.avro.random.generator.API;
import io.specmesh.blackbox.testharness.kafka.DockerKafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.KafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.clients.TestClients;
import io.specmesh.cli.Provision;
import io.specmesh.kafka.provision.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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
    void setUp() {}

    @AfterEach
    void tearDown() {}

    @Test
    void doThings() throws Exception {

        final int count = 100;

        generateSeedData(
                provisionSpec(
                        "build/resources/test/blackbox/seed/tube-passengers-api.yml",
                        "build/resources/test/blackbox"),
                count);

        provisionSpec(
                "build/resources/test/blackbox/simple_schema_demo-api.yml",
                "build/resources/test/blackbox");

        // Test produce/consume
        final int result =
                new Main()
                        .doThings(
                                KAFKA_ENV.kafkaBootstrapServers(),
                                KAFKA_ENV.schemeRegistryServer());
        assertThat(result, is(count));

        // sanity check schemas
        sanityCheckSchemaCreation();
    }

    private static void sanityCheckSchemaCreation() throws IOException, RestClientException {
        try (var srClient =
                io.specmesh.blackbox.testharness.kafka.clients.Clients.srClient(
                        KAFKA_ENV.schemeRegistryServer())) {
            final var allSubjects = srClient.getAllSubjects();
            assertThat("Should be 2 schemas in 2 subjects", allSubjects, hasSize(2));
            allSubjects.forEach(
                    subject -> {
                        try {
                            final var allVersions = srClient.getAllVersions(subject);
                            assertThat(
                                    "Should be 1 version of each schema", allVersions, hasSize(1));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private static void generateSeedData(final Status status, final int count) throws IOException {
        final var topicName = status.topics().iterator().next().name();

        final var api =
                new API(
                        count,
                        "id",
                        Files.readString(
                                Paths.get(
                                        "build/resources/test/blackbox/shouldProcessEvents_1/tube-passengers-set-1.avro")));

        try (var producer =
                TestClients.avroProducer(
                        KAFKA_ENV.kafkaBootstrapServers(),
                        KAFKA_ENV.schemeRegistryServer(),
                        "tube.passengers",
                        "admin",
                        new IntegerSerializer(),
                        GenericRecord.class,
                        new KafkaAvroSerializer(),
                        java.util.Map.of())) {
            api.run(
                    (key, genericRecord) -> {
                        try {
                            producer.send(
                                            new ProducerRecord<>(
                                                    topicName, (Integer) key, genericRecord))
                                    .get(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            producer.flush();
        }
    }

    private static Status provisionSpec(final String spec, final String schemaPath)
            throws Exception {
        final var provision =
                Provision.builder()
                        .brokerUrl(KAFKA_ENV.kafkaBootstrapServers())
                        .username(OWNER_USER)
                        .secret(OWNER_USER + "-secret")
                        .spec(spec)
                        .schemaPath(schemaPath)
                        .schemaRegistryUrl(KAFKA_ENV.schemeRegistryServer())
                        .build();
        provision.call();

        final var status = provision.state();
        assertThat("Expected 1 topic", status.topics(), hasSize(1));
        assertThat(
                "Topic failed to provision: " + status,
                status.topics().iterator().next().state(),
                is(Status.STATE.CREATED));
        return status;
    }
}
