package kafka.streams.globalktable.join;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KafkaStreamsGlobalKTable {

    public static void main(final String[] args) {
        SpringApplication.run(KafkaStreamsGlobalKTable.class, args);
    }

    @EnableScheduling
    @RestController
    @RequestMapping("user")
    public static class KStreamToTableJoinApplication {
        @Bean
        public Function<KStream<String, User>, KStream<String, User>> process() {
            return input -> input.map((key, user) -> new KeyValue<String, User>(user.getId(), user))
                    .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(User.class)))
                    .reduce((user1, user2) -> mergeObjects(user1, user2), Materialized.as("allusers")).toStream();
        }

        @Autowired
        private InteractiveQueryService interactiveQueryService;
        ReadOnlyKeyValueStore<String, User> userStore;

        @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
        public User user(@PathVariable final String id) {

            System.out.println("Inside the REST call:: ID is :" + id + " ");
            if (userStore == null) {
                userStore = interactiveQueryService.getQueryableStore("allusers",
                        QueryableStoreTypes.<String, User>keyValueStore());
            }

            final User user = userStore.get(id);
            if (user == null) {
                System.err.println("Error in getting users");
                throw new IllegalArgumentException("User not found.");
            }
            return new User(user.getId(), user.getName(), user.getAge());
        }

        @Scheduled(fixedRate = 5000, initialDelay = 5000)
        public void printUserTable() {
            if (userStore == null) {
                userStore = interactiveQueryService.getQueryableStore("allusers",
                        QueryableStoreTypes.<String, User>keyValueStore());
            }

            final KeyValueIterator<String, User> allusers = userStore.all();

            while (allusers.hasNext()) {
                final KeyValue<String, User> kv = allusers.next();
                System.out.println("User ID: " + kv.key + " user: " + userStore.get(kv.key).getName() + ":"
                        + userStore.get(kv.key).getAge() + " idname: " + userStore.get(kv.key).getIdname());

            }
            System.out.println("-----------------------------------------------");
        }

        @SuppressWarnings("unchecked")
        public <T> T mergeObjects(T first, T second) {
            Class<?> clazz = first.getClass();
            Field[] fields = clazz.getDeclaredFields();
            Object returnValue = null;
            try {
                returnValue = clazz.getDeclaredConstructor().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value1 = field.get(first);
                    Object value2 = field.get(second);
                    Object value = (value2 == null) ? value1 : value2;
                    field.set(returnValue, value);
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {

                e.printStackTrace();
            }
            return (T) returnValue;
        }
    }
}
