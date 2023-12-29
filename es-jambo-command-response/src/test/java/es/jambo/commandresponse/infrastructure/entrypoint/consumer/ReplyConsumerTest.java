package es.jambo.commandresponse.infrastructure.entrypoint.consumer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EmbeddedKafka(topics = {ReplyConsumer.TOPIC_COMMAND_REQUEST, ReplyConsumer.TOPIC_COMMAND_RESPONSE},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class ReplyConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    private ConsumerRecord<String, String> consumerRecord;

    @SpyBean
    private ReplyConsumer replyConsumer;

    @BeforeEach
    void initialize() {
        consumerRecord = null;
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void should_sendResponseMessage_when_recivedMessageCommand() {
        final var id = UUID.randomUUID().toString();
        final var requestMessage = "ping";
        final var expectedResponseMessage = "pong";

        sendCommandMessage(id, requestMessage);

        waitResponse();
        Assertions.assertThat(consumerRecord).isNotNull();
        Assertions.assertThat(consumerRecord.headers().lastHeader(ReplyConsumer.HEADER_ID)).isNotNull();
        Assertions.assertThat(consumerRecord.headers().lastHeader(ReplyConsumer.HEADER_CORRELATION_ID).value()).isEqualTo(id.getBytes());
        Assertions.assertThat(consumerRecord.value()).isEqualTo(expectedResponseMessage);
        Mockito.verify(replyConsumer, Mockito.times(1)).onMessage(Mockito.isA(ConsumerRecord.class));
    }

    private void sendCommandMessage(String id, String body) {
        final var record = new ProducerRecord<String, String>(ReplyConsumer.TOPIC_COMMAND_REQUEST, body);
        record.headers().add(ReplyConsumer.HEADER_REPLY_CHANNEL, ReplyConsumer.TOPIC_COMMAND_RESPONSE.getBytes())
                .add(ReplyConsumer.HEADER_ID, id.getBytes());
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }

    private void waitResponse() {
        Awaitility.await().pollInterval(100, TimeUnit.MILLISECONDS)
                .during(1, TimeUnit.SECONDS).until(() -> consumerRecord != null);
    }

    @KafkaListener(topics = {ReplyConsumer.TOPIC_COMMAND_RESPONSE})
    public void readResponse(ConsumerRecord<String, String> consumerRecord) {
        this.consumerRecord = consumerRecord;
    }

}