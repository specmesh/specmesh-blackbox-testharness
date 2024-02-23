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

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public abstract class AvroSerde<T> implements Serde<T> {

    public AvroSerde() {
    }

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
    }

    @Override
    public void close() {
        // No resources to close
    }

    @Override
    public Serializer<T> serializer() {
        return (Serializer<T>) new KafkaAvroSerializer();
    }

    @Override
    public Deserializer<T> deserializer() {
        return (Deserializer<T>) new AvroDeserializer<>();
    }

    private final class AvroDeserializer<T> extends KafkaAvroDeserializer {

        @Override
        public Object deserialize(final String topic, final Headers headers, final byte[] bytes) {
            return  convert(super.deserialize(topic, headers, bytes));
        }

        @Override
        public Object deserialize(final String topic, final byte[] bytes) {
            return  convert(super.deserialize(topic, bytes));
        }

        @Override
        public Object deserialize(final String topic, final byte[] bytes, final Schema readerSchema) {
            return  convert(super.deserialize(topic, bytes, readerSchema));
        }

        @Override
        public Object deserialize(String topic, Headers headers, byte[] bytes, Schema readerSchema) {
            return  convert(super.deserialize(topic, headers, bytes, readerSchema));
        }

        @Override
        protected Object deserialize(byte[] payload) throws SerializationException {
            return  convert(super.deserialize(payload));
        }

        @Override
        protected Object deserialize(byte[] payload, Schema readerSchema) throws SerializationException {
            return  convert(super.deserialize(payload, readerSchema));
        }

        @Override
        protected Object deserialize(String topic, Boolean isKey, byte[] payload, Schema readerSchema) throws SerializationException {
            return  convert(super.deserialize(topic, isKey, payload, readerSchema));
        }

        @Override
        protected Object deserialize(String topic, Boolean isKey, Headers headers, byte[] payload, Schema readerSchema) throws SerializationException {
            return convert(super.deserialize(topic, isKey, headers, payload, readerSchema));
        }
    }

    /**
     * Convert from GenericRecord to Pojo
     * @param genericRecordMaybe
     * @return
     */
    abstract protected T convert(Object genericRecordMaybe);
}
