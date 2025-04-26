## SyncKafkaService - Asynchronous to Synchronous Kafka Communication
This project implements a simple yet powerful mechanism to transform asynchronous Kafka communication into synchronous requests/responses using Java and Spring Boot. The primary use case is to handle requests in a request-response pattern between two services, leveraging Kafka as a message broker.

### Overview
In many systems, Kafka is used for asynchronous communication, but there are scenarios where synchronous request/response patterns are needed. This project bridges that gap by using Kafka for asynchronous messaging while synchronously waiting for a response from the consumer service. It handles sending requests to Kafka topics and waiting for responses using CompletableFuture, providing a clear, non-blocking approach to handle long-running operations.

### Key Features:
1. Asynchronous Request-Response: Uses Kafka to send requests to a topic, waits for a response using CompletableFuture.

2. Custom Kafka Consumer/Producer Configuration: Configures Kafka consumers and producers with Avro serialization and deserialization.

3. High Availability & Scalability: Built with Spring Boot and Kafka to handle high-throughput scenarios with proper scaling and reliability.

4. Avro Serialization: Utilizes Confluent’s Avro serializers/deserializers to ensure efficient data transfer.

### Architecture:
1. SyncKafkaService (Producer):
Sends messages to a Kafka topic and waits for a response in a blocking manner using CompletableFuture.
Handles request generation, correlation IDs, and waits for replies with a timeout.

2. ResponderKafkaListener (Consumer):
Listens for messages and processes them.
Sends a processed response back to a different Kafka topic.

3. Kafka Configuration:
Consumer and producer configurations are provided using Spring Kafka, with Avro serialization configured to handle data transmission between services.
Schema Registry integration ensures that Avro schemas are correctly used during serialization/deserialization.

### Usage:
1. Start Kafka Broker: Ensure you have a running Kafka broker with the necessary topics, you can rename. **Also you can use docker-compose in /infra**
2. Run Spring Boot Application:
SyncKafkaService will act as the producer and send a message to Kafka, waiting for a response.
ResponderKafkaListener will act as the consumer, processing messages and sending responses back.
3. Send Requests: You can use the SyncKafkaService.sendAndWait() method to send requests and wait for the response synchronously.
4. Configuration: Ensure the Kafka broker URL and schema registry URL are correctly set in your application.yml or application.properties.

### Dependencies:
Spring Boot
Spring Kafka
Confluent Kafka (for Avro Serialization)
Java 17+ (Recommended)
Apache Kafka





