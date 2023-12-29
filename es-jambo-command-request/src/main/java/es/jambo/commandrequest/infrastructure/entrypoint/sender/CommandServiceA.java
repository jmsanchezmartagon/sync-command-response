package es.jambo.commandrequest.infrastructure.entrypoint.sender;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
final class CommandServiceA {

    public static final String TOPIC_COMMAND_REQUEST = "command.request";
    public static final String TOPIC_COMMAND_RESPONSE = "command.response";

    @KafkaListener(topics = {TOPIC_COMMAND_REQUEST})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        System.out.println(consumerRecord.value());
    }
}
