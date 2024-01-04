package es.jambo.commandrequest.infrastructure.entrypoint.controller;
/*
import es.jambo.commandrequest.utils.CommandHeader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static es.jambo.commandrequest.utils.CommandSender.TOPIC_COMMAND_REQUEST;

@SpringBootTest
@Testcontainers
public class CommandControllerTest {

    private static KafkaContainer kafka;

    public static final String RESPONSE_DATA = UUID.randomUUID().toString();
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private CommandController commandController;
    @Value("${kafka.topic.command.response}")
    private String replyTo;

    @BeforeAll
    static void initialize() {
        DockerComposeContainer kafka = new DockerComposeContainer(new File("src/test/resources/test-docker-compose.yml").getAbsoluteFile())
                .withExposedService("kafka", 9092,
                        Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));
        //kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1")).withListener(() -> "localhost:9092");
        kafka.start();


     //   Awaitility.await().pollInterval(100, TimeUnit.MILLISECONDS)
      //          .during(1, TimeUnit.SECONDS).until(() -> kafka.isHealthy());

    }

    public static boolean isPortOpen() {
        try {
            Socket socket = new Socket("kafka", 9092);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    void should_recievedMessage_when_sendCommand() throws ExecutionException, InterruptedException {
        Awaitility.await().during(Durations.FIVE_SECONDS);
        final var response = commandController.executeCommand();
        Assertions.assertThat(response).isNotEmpty().contains(RESPONSE_DATA);
    }

    @KafkaListener(topics = {TOPIC_COMMAND_REQUEST}, autoStartup = "true")
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        Assertions.assertThat(consumerRecord.headers().lastHeader(CommandHeader.ID).value()).isNotEmpty();
        final var record = new ProducerRecord<String, String>(replyTo, CommandControllerTest.RESPONSE_DATA);
        record.headers().add(CommandHeader.CORRELATION_ID, consumerRecord.headers().lastHeader(CommandHeader.ID).value());
        final var result = kafkaTemplate.send(record);
        result.whenComplete((successResult, failException) ->
                Assertions.assertThat(failException).isNull()
        );
    }

}

*/

import java.util.UUID;

public class CommandControllerTest {

    public static final String RESPONSE_DATA = UUID.randomUUID().toString();
}
