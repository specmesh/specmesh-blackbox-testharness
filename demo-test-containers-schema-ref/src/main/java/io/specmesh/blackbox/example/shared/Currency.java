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

package io.specmesh.blackbox.example.shared;

import io.specmesh.blackbox.kafka.clients.AvroSerde;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Builder
public class Currency {
    private String symbol;
    private double amount;

    public static Serde<Currency> serde() {
        return new AvroSerde<>() {
            @Override
            public Currency convert(final Object genericRecordMaybe) {
                if (genericRecordMaybe instanceof GenericRecord) {
                    final var record = (GenericRecord) genericRecordMaybe;
                    return Currency.builder()
                            .symbol(record.get("symbol").toString())
                            .amount((Double) record.get("amount"))
                            .build();
                } else if (genericRecordMaybe instanceof Currency) {
                    return (Currency) genericRecordMaybe;
                } else {
                    throw new RuntimeException("Unexpected Object:" + genericRecordMaybe);
                }
            }
        };
    }
}
