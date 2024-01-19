package es.jambo.commandrequest.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class CommandContextTest {

    private static final CommandContext context = CommandContext.getInstance();

    @Test
    void should_persist_when_addCompletableTask() {
        final String idTask = UUID.randomUUID().toString();
        final var task = new CompletableFuture<ConsumerRecord<Integer, String>>();

        context.put(idTask, task);

        final var persistedTask = context.get(idTask);
        Assertions.assertThat(persistedTask).isNotNull().isEqualTo(task);
    }

    @Test
    void should_getNull_when_notExists() {
        final String idTask = UUID.randomUUID().toString();

        final var persistedTask = context.get(idTask);

        Assertions.assertThat(persistedTask).isNull();
    }

    @Test
    void should_getNull_when_wasConsumed() {
        final String idTask = UUID.randomUUID().toString();
        final var task = new CompletableFuture<ConsumerRecord<Integer, String>>();

        context.put(idTask, task);
        context.get(idTask);

        final var persistedTask = context.get(idTask);
        Assertions.assertThat(persistedTask).isNull();
    }

    @Test
    void should_getIllegalArgument_when_putTaskAsNull() {
        final String idTask = UUID.randomUUID().toString();

        Assertions.assertThatThrownBy(() ->
                context.put(idTask, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_getIllegalArgument_when_putIdasNull() {
        final var task = new CompletableFuture<ConsumerRecord<Integer, String>>();

        Assertions.assertThatThrownBy(() ->
                context.put(null, task)
        ).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void should_getIllegalArgument_when_getIdAsNull() {
        final var task = new CompletableFuture<ConsumerRecord<Integer, String>>();

        Assertions.assertThatThrownBy(() ->
                context.get(null)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}