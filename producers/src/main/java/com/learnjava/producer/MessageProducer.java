package com.learnjava.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    String topicName = "test-topic";
    KafkaProducer kafkaProducer;

    public MessageProducer(Map<String, String> producerProps) {
        kafkaProducer = new KafkaProducer(producerProps);
    }


    public void publishMessageSync(String key, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, message);
        RecordMetadata recordMetadata = null;
        try {
            recordMetadata = (RecordMetadata) kafkaProducer.send(producerRecord).get();
            logger.info("Message ** {} ** sent successfully with the key  **{}** and the recordMetadata is : {} ", message,key, recordMetadata);
        }
        catch (InterruptedException | ExecutionException e) {
            logger.error("Exception in publishMessageSync : {} ", e);
        }
        logger.info(" Published Record Offset is {} and the partition is {}", recordMetadata.offset(), recordMetadata.partition());
    }

    public void publishMessageAsync(String key, String message) throws InterruptedException, ExecutionException, TimeoutException {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, message);
        Future<RecordMetadata> recordMetadataFuture = kafkaProducer.send(producerRecord);
        RecordMetadata recordMetadata = recordMetadataFuture.get(3, TimeUnit.SECONDS);
        logger.info("Record MetaData Offset : {} and the partition is {}", recordMetadata.offset(), recordMetadata.partition());
    }

    public static Map<String, String> buildProducerProperties() {

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
        propsMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return propsMap;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

        Map<String, String> producerProps = buildProducerProperties();
        MessageProducer messageProducer = new MessageProducer(producerProps);
        messageProducer.publishMessageSync(null, "ABC");
        messageProducer.publishMessageAsync(null , "ABC");

    }
}