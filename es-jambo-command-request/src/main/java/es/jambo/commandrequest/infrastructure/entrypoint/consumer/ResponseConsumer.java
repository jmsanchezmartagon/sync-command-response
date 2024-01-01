package es.jambo.commandrequest.infrastructure.entrypoint.consumer;

import es.jambo.commandrequest.utils.CommandContext;
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
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        final var correlationId = new String(consumerRecord.headers().lastHeader("correlation_id").value());
        CommandContext.getInstance().get(correlationId).complete(consumerRecord);
    }
}
