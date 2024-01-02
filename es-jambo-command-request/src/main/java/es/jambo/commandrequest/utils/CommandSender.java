package es.jambo.commandrequest.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.jambo.commandrequest.utils.CommandHeader.ID;
import static es.jambo.commandrequest.utils.CommandHeader.REPLY_CHANNEL;

@Component
public class CommandSender {
    private static final Logger logger = LoggerFactory.getLogger(CommandSender.class);
    public static final String TOPIC_COMMAND_REQUEST = "command.request";

    @Value("${kafka.topic.command.response}")
    public String replyTo;
    private final KafkaTemplate<Integer, String> kafkaTemplate;

    CommandSender(KafkaTemplate<Integer, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public ConsumerRecord<Integer, String> executeCommand(String message) throws ExecutionException, InterruptedException {

        final var id = UUID.randomUUID().toString();

        final var commandMessage = new ProducerRecord<Integer, String>(TOPIC_COMMAND_REQUEST, message);
        commandMessage.headers().add(REPLY_CHANNEL, replyTo.getBytes())
                .add(ID, id.getBytes());
        var result = kafkaTemplate.send(commandMessage).get();
        logger.debug("Sent topic:{} partition:{}", result.getProducerRecord().topic(),
                result.getRecordMetadata().partition());


        final var completableFuture = new CompletableFuture<ConsumerRecord<Integer, String>>();

        CommandContext.getInstance().put(id, completableFuture);

        return completableFuture.thenApply(readRecord -> readRecord).get();
    }
}
