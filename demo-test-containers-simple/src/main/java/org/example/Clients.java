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

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Clients {

    public static <K, V> Consumer<K, V> avroConsumer(
            String domainId, String bootstrap, String srUrl, final String topicName, final String userName,
            Class<K> keyClass, final Deserializer<K> keyDeserializer, Class<V> valueClass,  final Deserializer<V> valueDeserializer) throws Exception {
        return consumer(
                domainId,
                bootstrap,
                srUrl,
                topicName,
                keyClass,
                keyDeserializer,
                valueClass,
                valueDeserializer,
                userName,
                Map.of());
    }


    private static <K, V> Consumer<K, V> consumer(
            final String domainId,
            final String boostrap,
            final String srUrl,
            final String topicName,
            final Class<K> keyClass,
            final Deserializer<K> keyDeserializer,
            final Class<V> valueClass,
            final Deserializer<V> valueDeserializer,
            final String userName,
            final Map<String, Object> providedProps) throws Exception {

        final Map<String, Object> avroProps = new HashMap<>(Map.of(
                // change for common data types
                // "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy",
                "schema.reflection", "false"));

        final Map<String, Object> props =
                io.specmesh.kafka.Clients.consumerProperties(
                        domainId,
                        "test-consumer-" + domainId,
                        boostrap,
                        srUrl,
                        keyDeserializer.getClass(),
                        valueDeserializer.getClass(),
                        true,
                        providedProps,
                        avroProps);

        props.putAll(io.specmesh.kafka.Clients.clientSaslAuthProperties(userName, userName + "-secret"));
        props.put(CommonClientConfigs.GROUP_ID_CONFIG, "test-consumer-" + domainId + "-" +userName + "-"+UUID.randomUUID());

        valueDeserializer.configure(props, false);
        final var consumer = new KafkaConsumer<>(props, keyDeserializer, valueDeserializer);
        consumer.subscribe(List.of(topicName));
        return consumer;
    }
    public static <K, V> Producer<K, V> avroProducer(

            String bootstrapUrl, String srUrl, final String domainId, final String user,
            final Serializer<K> keySerializer,
            final Class<V> valueClass,
            final Serializer<?> valueSerializer,
            final Map<String, Object> props) {
        HashMap<String, Object> propsMap = new HashMap<>(Map.of(
                // change for common data types
                // "value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy",
                //"value.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
                "schema.reflection", "false"));
        propsMap.putAll(props);
        return producer(domainId, bootstrapUrl, srUrl, keySerializer, valueClass, valueSerializer, user, propsMap);
    }

    private static <K, V> Producer<K, V> producer(
            final String domainId,
            final String bootstrapUrl,
            final String srurl,
            final Serializer<K> keySerializer,
            final Class<V> valueClass,
            final Serializer<?> valueSerializer,
            final String userName,
            final Map<String, Object> additionalProps) {
        final Map<String, Object> props =
                io.specmesh.blackbox.testharness.kafka.clients.Clients.producerProperties(
                        domainId,
                        UUID.randomUUID().toString(),
                        bootstrapUrl,
                        srurl,
                        keySerializer.getClass(),
                        valueSerializer.getClass(),
                        false,
                        additionalProps);

        props.putAll(io.specmesh.blackbox.testharness.kafka.clients.Clients.clientSaslAuthProperties(userName, userName + "-secret"));
        valueSerializer.configure(props, false);
        return new KafkaProducer<>(props, keySerializer, (Serializer<V>) valueSerializer);
    }


}
