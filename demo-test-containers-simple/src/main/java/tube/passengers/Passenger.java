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

package tube.passengers;

import io.specmesh.blackbox.testharness.kafka.clients.AvroSerde;
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
public class Passenger {
    private int id;
    private long time;
    private String username;
    private String email;
    private int age;

    public static Serde<Passenger> serde() {
        return new AvroSerde<>() {
            @Override
            public Passenger convert(final Object genericRecordMaybe) {
                if (genericRecordMaybe instanceof GenericRecord) {
                    final var record = (GenericRecord) genericRecordMaybe;
                    return Passenger.builder()
                            // avro parser limitation on 'int's from json (need to modify
                            // ObjectMapper)
                            .id(((Long) record.get("id")).intValue())
                            .time((Long) record.get("time"))
                            .username(record.get("username").toString())
                            .email(record.get("email").toString())
                            .age((Integer) record.get("age"))
                            .build();
                } else if (genericRecordMaybe instanceof Passenger) {
                    return (Passenger) genericRecordMaybe;
                } else {
                    throw new RuntimeException("Unexpected Object:" + genericRecordMaybe);
                }
            }
        };
    }
}
