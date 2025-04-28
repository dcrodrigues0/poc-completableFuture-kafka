package com.acme.async_to_sync.service;

import com.test.syncapi.avro.MessageRequest;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class SyncKafkaService {

    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;
    public final Map<UUID, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    private final String requestTopic = "comando";
    private final String responseTopic = "resposta-comando";

    public SyncKafkaService(KafkaTemplate<String, MessageRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String sendAndWait(String someMessage) throws Exception {
        UUID correlationId = UUID.randomUUID();

        // Cria a mensagem AVRO
        MessageRequest avroMessage = MessageRequest.newBuilder()
                .setCorrelationId(correlationId)
                .setSomeMessage(someMessage)
                .build();

        // Registra o future para aguardar resposta
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        // Envia para o Kafka
        kafkaTemplate.send(requestTopic, avroMessage);

        try {
            // Espera a resposta com timeout
            return future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingRequests.remove(correlationId);
            throw new RuntimeException("Timeout esperando a resposta Kafka");
        }
    }

    // Recebe a resposta (presumivelmente outro Avro com o mesmo correlationId)
    @KafkaListener(topics = responseTopic, groupId = "sync-service", containerFactory = "kafkaListenerContainerFactory")
    public void listenResponse(GenericRecord record) {
        // Extrai o correlationId
        String correlationId = record.get("correlationId").toString();
        String message = record.get("someMessage").toString();

        CompletableFuture<String> future = pendingRequests.remove(UUID.fromString(correlationId));
        if (future != null) {
            future.complete(message);
        }
    }
}
