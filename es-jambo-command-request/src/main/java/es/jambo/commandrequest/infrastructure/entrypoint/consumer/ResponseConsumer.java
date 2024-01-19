package es.jambo.commandrequest.infrastructure.entrypoint.consumer;

import es.jambo.commandrequest.utils.CommandContext;
import es.jambo.commandrequest.utils.CommandHeader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
@Component
final class ResponseConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ResponseConsumer.class);

    @KafkaListener(topics = {"${kafka.topic.command.response}"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        final var correlationId = getCorrelationID(consumerRecord);
        completeAction(correlationId, consumerRecord);
    }

    private String getCorrelationID(ConsumerRecord<Integer, String> consumerRecord) {
        final var header = consumerRecord.headers().lastHeader(CommandHeader.CORRELATION_ID);
        if (header == null) {
            logger.error("Error to process message without correlation id header, message data: {}", consumerRecord);
            throw new IllegalArgumentException("Header: Correlation_id is required");
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private void completeAction(String correlationId, ConsumerRecord<Integer, String> consumerRecord) {
        final var action = CommandContext.getInstance().get(correlationId);
        if (action == null) {
            logger.error("Error to process message, no action for id. {}", correlationId);
            throw new IllegalAccessError(String.format("There is NO action for id: %s", correlationId));
        }
        action.complete(consumerRecord);
    }
}
