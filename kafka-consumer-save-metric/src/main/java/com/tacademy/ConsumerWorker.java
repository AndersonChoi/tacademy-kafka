package com.tacademy;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerWorker implements Runnable {
    private Properties prop;
    private String topic;
    private String threadName;
    private KafkaConsumer<String, String> consumer;

    ConsumerWorker(Properties prop, String topic, int number) {
        this.prop = prop;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(prop);
        consumer.subscribe(Arrays.asList(topic));
        File file = new File(threadName + ".csv");

        try {
            while (true) {
                FileWriter fw = new FileWriter(file, true);
                StringBuilder fileWriteBuffer = new StringBuilder();
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    fileWriteBuffer.append(record.value()).append("\n");
                }
                fw.write(fileWriteBuffer.toString());
                consumer.commitSync();
                fw.close();
            }
        } catch (IOException e) {
            System.err.println(threadName + " IOException"+e);
        } catch (WakeupException e) {
            System.out.println(threadName + " WakeupException");
        } finally {
            consumer.commitSync();
            consumer.close();
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }
}