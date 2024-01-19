package es.jambo.commandresponse.infrastructure.entrypoint.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
final class ReplyConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ReplyConsumer.class);
    public static final String HEADER_ID = "id";
    public static final String HEADER_REPLY_CHANNEL = "reply_channel";
    public static final String HEADER_CORRELATION_ID = "correlation_id";
    public static final String TOPIC_COMMAND_REQUEST = "command.request";
    public static final String TOPIC_COMMAND_RESPONSE = "command.response";
    @Value("${nodename}")
    public String nodeName;
    private KafkaTemplate<String, String> kafkaTemplate;

    public ReplyConsumer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = {TOPIC_COMMAND_REQUEST})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        final var correlationId = consumerRecord.headers().lastHeader(HEADER_ID);
        final var topic = consumerRecord.headers().lastHeader(HEADER_REPLY_CHANNEL);

        final var responseRecord = new ProducerRecord<String, String>(new String(topic.value(), StandardCharsets.UTF_8), String.format("%s: pong", nodeName));
        responseRecord.headers().add(HEADER_ID, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        responseRecord.headers().add(HEADER_CORRELATION_ID, correlationId.value());

        final var result = kafkaTemplate.send(responseRecord);
        result.whenComplete((successResult, failException) -> {
            if (failException != null) {
                logger.error("Fail to send message {}", failException.getMessage(), failException);
            } else {
                logger.info("sended key: {} and value: {} to partition: {}", successResult.getProducerRecord().key(), successResult.getProducerRecord().value(), successResult.getRecordMetadata().partition());
            }
        });
    }
}
