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

package io.specmesh.blackbox;

import io.specmesh.blackbox.example.shared.Currency;
import io.specmesh.blackbox.kafka.clients.AvroSerde;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.avro.generic.GenericRecord;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Builder
public class Trade {
    private String id;
    private String detail;
    private Currency currency;

    public static AvroSerde<Trade> serde() {
        return new AvroSerde<>() {
            @Override
            public Trade convert(final Object genericRecordMaybe) {
                if (genericRecordMaybe instanceof GenericRecord) {
                    final var record = (GenericRecord) genericRecordMaybe;
                    return Trade.builder()
                            .id(record.get("id").toString())
                            .detail(record.get("detail").toString())
                            .currency(
                                    ((AvroSerde<Currency>) Currency.serde())
                                            .convert(record.get("currency")))
                            .build();
                } else if (genericRecordMaybe instanceof Trade) {
                    return (Trade) genericRecordMaybe;
                } else {
                    throw new RuntimeException("Unexpected Object:" + genericRecordMaybe);
                }
            }
        };
    }
}
