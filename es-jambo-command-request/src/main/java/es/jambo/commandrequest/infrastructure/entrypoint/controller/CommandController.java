package es.jambo.commandrequest.infrastructure.entrypoint.controller;

import es.jambo.commandrequest.utils.CommandContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController()
@RequestMapping("/command")
class CommandController {

    public static final String HEADER_ID = "id";
    public static final String HEADER_REPLY_CHANNEL = "reply_channel";
    public static final String TOPIC_COMMAND_REQUEST = "command.request";
    private static Logger logger = LoggerFactory.getLogger(CommandController.class);
    @Value("${kafka.topic.command.response}")
    public String replyTo;
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public CommandController(KafkaTemplate<Integer, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/")
    public String executeCommand() throws ExecutionException, InterruptedException {
        final var exampleMessage = "ping";
        final var id = UUID.randomUUID().toString();

        final var commandMessage = new ProducerRecord<Integer, String>(TOPIC_COMMAND_REQUEST, exampleMessage);
        commandMessage.headers().add(HEADER_REPLY_CHANNEL, replyTo.getBytes())
                .add(HEADER_ID, id.getBytes());
        var result = kafkaTemplate.send(commandMessage).get();
        logger.debug("Sent topic:{} partition:{}", result.getProducerRecord().topic(),
                result.getRecordMetadata().partition());


        final var completableFuture = new CompletableFuture<ConsumerRecord<Integer, String>>();

        CommandContext.getInstance().put(id, completableFuture);

        final var responseRecord = completableFuture.thenApply(readRecord -> readRecord).get();
        return responseRecord.toString();
    }
}
