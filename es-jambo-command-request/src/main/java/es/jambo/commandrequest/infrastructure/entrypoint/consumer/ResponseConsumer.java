package es.jambo.commandrequest.infrastructure.entrypoint.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author jmsanchez
 */
@Component
final class ResponseConsumer {
    public static final String TOPIC_COMMAND_RESPONSE = "command.response";

    @KafkaListener(topics = {"${kafka.topic.command.response}"})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        System.out.println(consumerRecord.value());
    }
}
