package es.jambo.commandrequest.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
@SpringBootTest
@EmbeddedKafka(topics = {CommandSender.TOPIC_COMMAND_REQUEST, "${kafka.topic.command.response}"},
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092", "auto.create.topics.enable=true"})
class CommandSenderTest {

    public static final String RESPONSE_DATA = UUID.randomUUID().toString();
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.topic.command.response}")
    private String replyTo;
    @Autowired
    private CommandSender commandSender;


    @Test
    void should_recievedMessage_when_sendCommand() throws ExecutionException, InterruptedException {
        Awaitility.await().until(this::isKafkaReady);
        final var response = commandSender.executeCommand("ping");
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.value()).isNotNull().contains(RESPONSE_DATA);
    }


    @KafkaListener(topicPartitions = {
            @TopicPartition(topic = CommandSender.TOPIC_COMMAND_REQUEST,
                    partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0"))})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        Assertions.assertThat(consumerRecord.headers().lastHeader(CommandHeader.ID).value()).isNotEmpty();

        final var record = new ProducerRecord<String, String>(replyTo, RESPONSE_DATA);
        record.headers().add(CommandHeader.CORRELATION_ID, consumerRecord.headers().lastHeader(CommandHeader.ID).value());
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }

    private boolean isKafkaReady() {
        try {
            return kafkaTemplate.send("test", "ping").get().getRecordMetadata() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}