package kafka.streams.globalktable.join;

import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id;
    private String name;
    private Integer age;
    private final String idname;

    public User(@JsonProperty("id") final String id, @JsonProperty("name") final String name, @JsonProperty("age") final Integer age) {
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

    /**
     * Fill current object fields with new object values, ignoring new NULLs. Old
     * values are overwritten.
     *
     * @param newObject Same type object with new values.
     */
    public void merge(final Object newObject) {
        assert this.getClass().getName().equals(newObject.getClass().getName());
        for (final Field field : this.getClass().getDeclaredFields()) {
            for (final Field newField : newObject.getClass().getDeclaredFields()) {
                if (field.getName().equals(newField.getName())) {
                    try {
                        field.set(this, newField.get(newObject) == null ? field.get(this) : newField.get(newObject));
                    } catch (final IllegalAccessException ignore) {
                    }
                }
            }
        }
    }

}