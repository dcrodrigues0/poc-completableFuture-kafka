package com.acme.async_to_sync.service;

import com.test.syncapi.avro.MessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SyncKafkaServiceTest {

    @Mock
    private KafkaTemplate<String, MessageRequest> kafkaTemplate;

    private SyncKafkaService service;

    @BeforeEach
    void setUp() {
        service = new SyncKafkaService(kafkaTemplate);
    }

    @Test
    void shouldSendMessageAndReturnResponse() throws Exception {
        String expectedResponse = "world!";
        String sentMessage = "hello";

        // simula chamada ao método listen depois de enviar
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            await().atMost(1, TimeUnit.SECONDS).until(() -> !service.pendingRequests.isEmpty());;

            // capturando correlationId da pendingRequests
            UUID correlationId = service.pendingRequests.keySet().iterator().next();

            MessageRequest mockResponse = MessageRequest.newBuilder()
                    .setCorrelationId(correlationId)
                    .setSomeMessage(expectedResponse)
                    .build();

            service.listenResponse(mockResponse);
        }, 5, TimeUnit.SECONDS);

        String result = service.sendAndWait(sentMessage);
        assertEquals(expectedResponse, result);
    }

    @Test
    void shouldTimeoutIfNoResponse() {
        String sentMessage = "timeout-test";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.sendAndWait(sentMessage);
        });

        assertTrue(exception.getMessage().contains("Timeout"));
    }

    @Test
    void shouldStoreFutureUntilResponseArrives() throws Exception {
        String sentMessage = "test";

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            UUID correlationId = service.pendingRequests.keySet().iterator().next();

            MessageRequest mockResponse = MessageRequest.newBuilder()
                    .setCorrelationId(correlationId)
                    .setSomeMessage("response")
                    .build();

            service.listenResponse(mockResponse);
        }, 100, TimeUnit.MILLISECONDS);

        String result = service.sendAndWait(sentMessage);

        assertEquals("response", result);
        assertTrue(service.pendingRequests.isEmpty());
    }
}