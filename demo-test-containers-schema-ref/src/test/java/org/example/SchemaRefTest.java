package org.example;

import com.example.trades.Clients;
import com.example.trades.Main;
import com.example.trades.Trade;
import common.example.shared.Currency;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.Metadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.RuleSet;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.specmesh.avro.random.generator.API;
import io.specmesh.blackbox.testharness.kafka.DockerKafkaEnvironment;
import io.specmesh.blackbox.testharness.kafka.KafkaEnvironment;
import io.specmesh.cli.Provision;
import io.specmesh.kafka.provision.Status;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SchemaRefTest {

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
    void validateSerialisationWorksBothWays() throws Exception {
        var currencySchemaString = Files.readString(Path.of("./build/resources/test/blackbox/schema/com.example.shared.Currency.avsc"));
        var tradeSchemaString = Files.readString(Path.of("./build/resources/test/blackbox/schema/com.example.trading.Trade.avsc"));

        var currencySchema = new AvroSchema(
                currencySchemaString
        );

        Metadata metadata = new Metadata(null, null, null);
        RuleSet ruleSet = new RuleSet(null, null);
        AvroSchema tradeSchema = new AvroSchema(tradeSchemaString,
                List.of(new SchemaReference("Currency", "com.example.shared.Currency", -1)),
                Map.of("com.example.shared.Currency", currencySchemaString),
                metadata, ruleSet, 1, true);

        var srClient = io.specmesh.blackbox.testharness.kafka.clients.Clients.srClient(KAFKA_ENV.schemeRegistryServer());

        //  register both schemas prior for SerDe to work
        int currencyId = srClient.register("com.example.shared.Currency", currencySchema);
        int tradeId = srClient.register("com.example.trading.Trade", tradeSchema);

        var serde = Trade.serde();


        Serializer<Trade> serializer = serde.serializer(
                srClient,
                Map.of(
                        // force schema registry
                        KafkaAvroSerializerConfig.SCHEMA_REFLECTION_CONFIG, "true",
                        KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, KAFKA_ENV.schemeRegistryServer()
                ));

        var payload = serializer.serialize("stuff", Trade.builder()
                .id("111")
                        .detail("doing stuff")
                .currency(Currency.builder()
                        .symbol("GBP")
                        .amount(123.456)
                        .build())
                .build());

        var stuff = serde.deserializer(srClient).deserialize("stuff", payload);


        assertThat("nested Currency not found", stuff.currency(), is (notNullValue()));
        assertThat("Payment detail not passed", stuff.detail(), is ("doing stuff"));
    }


    @Test
    void proveThatReferencedSchemasPropagate() throws Exception {

        final int count = 100;

        generateSeedData(
                provisionSpec("build/resources/test/blackbox/seed/com.example.shared-api.yml",
                        "build/resources/test/blackbox"),
                count);

        provisionSpec("build/resources/test/blackbox/com.example.trading-api.yml",
                "build/resources/test/blackbox");

        showThatCurrentAndPaymentAreProcessed(count);

        verifyTradesAreJoined(count);
    }

    private static void verifyTradesAreJoined(int count) throws Exception {
        Consumer<String, Trade> tradeConsumer = consumer(KAFKA_ENV.kafkaBootstrapServers(), KAFKA_ENV.schemeRegistryServer(), "admin",
                "com.example.trading._public.trade", "admin");
        final var records = tradeConsumer.poll(Duration.ofSeconds(10));
        assertThat(records.count(), is(count));
    }

    private static void showThatCurrentAndPaymentAreProcessed(int count) throws Exception {
        int result = new Main().doThings(KAFKA_ENV.kafkaBootstrapServers(), KAFKA_ENV.schemeRegistryServer());
        assertThat(result, is(count));
    }


    private static void generateSeedData(final Status status, final int count) throws IOException {
        final var topicName = status.topics().iterator().next().name();

        final var api = new API(count, "symbol", Files.readString(Paths.get(
                "build/resources/test/blackbox/seed/com.example.shared.Currency.avro")));

        try (var producer = Clients
                .avroProducer(KAFKA_ENV.kafkaBootstrapServers(), KAFKA_ENV.schemeRegistryServer(),
                        "com.example.trading", "admin", new StringSerializer(),
                        GenericRecord.class, new KafkaAvroSerializer(), java.util.Map.of(
                                KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.RecordNameStrategy"
                        ))
        ){
            api.run((key, genericRecord) -> {
                try {
                    System.out.println("Seed >>" + topicName + " rec:" + genericRecord);
                    producer.send(
                                    new ProducerRecord<>(topicName,
                                            key.toString(), genericRecord))
                            .get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            producer.flush();
        }
    }

    private static Status provisionSpec(final String spec, final String schemaPath) throws Exception {
        final var provision = Provision.builder()
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
        assertThat("Topic failed to provision: " +status, status.topics().iterator().next().state(),
                is(Status.STATE.CREATED));
        return status;
    }

    private static Consumer<String, Trade> consumer(String bootstrapUrl, String srUrl, String domainId,
                                                    String inputTopic, String username) throws Exception {
        return Clients.avroConsumer(domainId, bootstrapUrl, srUrl,
                inputTopic, username, String.class, new StringDeserializer(),
                Trade.class, Trade.serde().deserializer());
    }

}