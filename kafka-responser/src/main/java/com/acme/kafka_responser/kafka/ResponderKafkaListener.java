package com.acme.kafka_responser.kafka;

import com.test.syncapi.avro.MessageRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResponderKafkaListener {

    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public ResponderKafkaListener(KafkaTemplate<String, MessageRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "comando", groupId = "responder-service")
    public void listen(ConsumerRecord<String, MessageRequest> record) {
        MessageRequest request = record.value();
        System.out.println("Request recebido correlationId: " + request.getCorrelationId());

        MessageRequest response = MessageRequest.newBuilder()
                .setCorrelationId(request.getCorrelationId())
                .setSomeMessage("Processado: " + request.getSomeMessage())
                .build();

        kafkaTemplate.send("resposta-comando", response);
    }
}
