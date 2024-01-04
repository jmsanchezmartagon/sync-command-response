package es.jambo.commandrequest.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@EmbeddedKafka(topics = {CommandSender.TOPIC_COMMAND_REQUEST, "${kafka.topic.command.response}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092", "auto.create.topics.enable=true"})
class CommandSenderTest {

    public static final String RESPONSE_DATA = UUID.randomUUID().toString();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.topic.command.response}")
    private String replyTo;
    @Autowired
    private CommandSender commandSender;


    @Test
    void should_recievedMessage_when_sendCommand() throws ExecutionException, InterruptedException {

        final var response = commandSender.executeCommand("ping");
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.value()).isNotNull().contains(RESPONSE_DATA);
    }


    @KafkaListener(topics = {CommandSender.TOPIC_COMMAND_REQUEST})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        Assertions.assertThat(consumerRecord.headers().lastHeader(CommandHeader.ID).value()).isNotEmpty();

        final var record = new ProducerRecord<String, String>(replyTo, RESPONSE_DATA);
        record.headers().add(CommandHeader.CORRELATION_ID, consumerRecord.headers().lastHeader(CommandHeader.ID).value());
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }
}