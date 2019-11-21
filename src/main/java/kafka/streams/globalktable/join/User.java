package kafka.streams.globalktable.join;

import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id;
    private String name;
    private Integer age;

    public User(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("age") Integer age) {
        super();
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Fill current object fields with new object values, ignoring new NULLs. Old
     * values are overwritten.
     *
     * @param newObject Same type object with new values.
     */
    public void merge(Object newObject) {

        assert this.getClass().getName().equals(newObject.getClass().getName());

        for (Field field : this.getClass().getDeclaredFields()) {

            for (Field newField : newObject.getClass().getDeclaredFields()) {

                if (field.getName().equals(newField.getName())) {

                    try {

                        field.set(this, newField.get(newObject) == null ? field.get(this) : newField.get(newObject));

                    } catch (IllegalAccessException ignore) {
                        // Field update exception on final modifier and other cases.
                    }
                }
            }
        }
    }

}