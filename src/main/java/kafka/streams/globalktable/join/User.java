package kafka.streams.globalktable.join;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id;
    private String name;
    private Integer age;
    private final String idname;

    public User() {
        super();
        this.id = null;
        this.name = null;
        this.age = null;
        this.idname = null;
    }

    public User(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("age") Integer age) {
        super();
        this.id = id;
        this.name = name;
        this.age = age;
        this.idname = id + "_" + name;
    }

    public String getName() {
        return name;
    }

    public String getIdname() {
        return idname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }
}