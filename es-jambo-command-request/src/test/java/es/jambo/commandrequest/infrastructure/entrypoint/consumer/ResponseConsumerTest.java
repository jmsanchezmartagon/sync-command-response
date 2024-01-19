package es.jambo.commandrequest.infrastructure.entrypoint.consumer;

import es.jambo.commandrequest.utils.CommandContext;
import es.jambo.commandrequest.utils.CommandHeader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
@SpringBootTest
@EmbeddedKafka(topics = {"${kafka.topic.command.response}"},
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092", "auto.create.topics.enable=true"})
class ResponseConsumerTest {
    @Value("${kafka.topic.command.response}")
    private String responseTopic;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @SpyBean
    private ResponseConsumer responseConsumer;

    @BeforeEach
    void initialize() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void should_reciveMessage_when_topicHasMessages() {
        final var id = UUID.randomUUID().toString();
        final var requestMessage = "ping";

        final var completableFuture = new CompletableFuture<ConsumerRecord<Integer, String>>();
        CommandContext.getInstance().put(id, completableFuture);
        completableFuture.thenApply(readRecord ->
                Assertions.assertThat(readRecord.headers().lastHeader(CommandHeader.CORRELATION_ID).value()).isEqualTo(id.getBytes(StandardCharsets.UTF_8)));

        sendCommandMessage(id, requestMessage);

        waitResponse(completableFuture);
        Assertions.assertThat(completableFuture.isDone()).isTrue();
        Mockito.verify(responseConsumer, Mockito.times(1)).onMessage(Mockito.isA(ConsumerRecord.class));
    }

    private void sendCommandMessage(String correlationId, String body) {
        final var record = new ProducerRecord<String, String>(responseTopic, body);

        record.headers().add(CommandHeader.CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8));
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }

    private void waitResponse(final Future action) {
        Awaitility.await().pollInterval(100, TimeUnit.MILLISECONDS)
                .during(1, TimeUnit.SECONDS).until(() -> action.isDone());
    }

}