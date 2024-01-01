package es.jambo.commandrequest.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class CommandContext {
    private static CommandContext instance;
    private final Map<String, CompletableFuture<ConsumerRecord<Integer, String>>> context = Collections.synchronizedMap(new HashMap<>());

    private CommandContext() {
    }

    public static CommandContext getInstance() {
        if (instance == null) {
            instance = new CommandContext();
        }
        return instance;
    }

    public void put(String id, CompletableFuture<ConsumerRecord<Integer, String>> futureTask) {
        context.put(id, futureTask);
    }

    public CompletableFuture<ConsumerRecord<Integer, String>> get(String id) {
        return context.remove(id);
    }
}
