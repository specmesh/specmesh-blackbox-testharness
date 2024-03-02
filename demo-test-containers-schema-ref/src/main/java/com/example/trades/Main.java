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

package com.example.trades;

import common.example.shared.Currency;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(final String[] args) {
    }

    public int doThings(final String bootstrapUrl, final String srUrl) throws Exception {

        final var domainId = "com.example.trading";
        final var inputTopic = "com.example.shared._public.currency";
        final var outputTopic = "com.example.trading._public.trade";
        final var username = "admin";
        final var result = new AtomicInteger();


        try (Consumer<String, Currency> consumer = consumer(bootstrapUrl, srUrl, domainId, inputTopic, username);
             Producer<String, Trade> producer = producer(bootstrapUrl, srUrl, domainId, username)
        ){

            final var records = consumer.poll(Duration.ofSeconds(10));
            records.forEach(record -> {
                producer.send(new ProducerRecord<>(
                        outputTopic,
                        record.key(),
                                Trade.builder()
                                        .id(record.value().symbol()+"thing")
                                        .detail("booked")
                                        .currency(record.value())
                                        .build()));
                        try {
                            System.out.println("Transformed: " + record.value());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            result.addAndGet(records.count());
        }
        return result.get();
    }

    private static Producer<String, Trade> producer(String bootstrapUrl, String srUrl, String domainId, String username) {
        return Clients.avroProducer(
                bootstrapUrl, srUrl, domainId, username, new StringSerializer(), Trade.class, new KafkaAvroSerializer(),
                Map.of(
                        // change for common data types
//                              "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy",
//                             "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
                        // disables schema reflect and requires encoding
//                             "schema.reflection", "false",
                        // enable when sending pojo -
                        "schema.reflection", "true",
                        // force schema validation on write
                        "validate", "true")

        );
    }

    private static Consumer<String, Currency> consumer(String bootstrapUrl, String srUrl, String domainId,
                                                        String inputTopic, String username) throws Exception {
        return Clients.avroConsumer(domainId, bootstrapUrl, srUrl,
                inputTopic, username, String.class, new StringDeserializer(), Currency.class,
                Currency.serde().deserializer());
    }
}
