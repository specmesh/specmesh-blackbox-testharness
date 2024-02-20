package tube.passengers;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

public class Passenger extends SpecificRecordBase implements SpecificRecord {
    public static final Schema SCHEMA$ = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"passenger\",\"namespace\":\"tube.passengers\",\"fields\":[{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"username\",\"type\":\"string\"},{\"name\":\"email\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"}]}");

    private long time;
    private long id;
    private String username;
    private String email;
    private int age;

    public Passenger() {
    }

    public Passenger(long time, long id, String username, String email, int age) {
        this.time = time;
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA$;
    }

    @Override
    public Object get(int field$) {
        switch (field$) {
            case 0: return time;
            case 1: return id;
            case 2: return username;
            case 3: return email;
            case 4: return age;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    @Override
    public void put(int field$, Object value$) {
        switch (field$) {
            case 0: time = (Long) value$; break;
            case 1: id = (Long) value$; break;
            case 2: username = (String) value$; break;
            case 3: email = (String) value$; break;
            case 4: age = (Integer) value$; break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "time=" + time +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}
