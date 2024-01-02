package es.jambo.commandrequest.infrastructure.entrypoint.controller;

import es.jambo.commandrequest.utils.CommandHeader;
import es.jambo.commandrequest.utils.CommandSender;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@EmbeddedKafka(topics = {CommandSender.TOPIC_COMMAND_REQUEST, "${kafka.topic.command.response}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"},
        partitions = 1)
public class CommandControllerTest {


    public static final String RESPONSE_DATA = UUID.randomUUID().toString();
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private CommandController commandController;
    @Value("${kafka.topic.command.response}")
    private String replyTo;

    @Test
    void should_recievedMessage_when_sendCommand() throws ExecutionException, InterruptedException {
        final var response = commandController.executeCommand();
        Assertions.assertThat(response).isNotEmpty().contains(RESPONSE_DATA);
    }

    @KafkaListener(topics = {CommandSender.TOPIC_COMMAND_REQUEST})
    public void readResponse(ConsumerRecord<String, String> consumerRecord) {
        Assertions.assertThat(consumerRecord.headers().lastHeader(CommandHeader.ID).value()).isNotEmpty();

        final var record = new ProducerRecord<String, String>(replyTo, CommandControllerTest.RESPONSE_DATA);
        record.headers().add(CommandHeader.CORRELATION_ID, consumerRecord.headers().lastHeader(CommandHeader.ID).value());
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }

}