package com.example.onlineCodeSubmitter.producers;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.onlineCodeSubmitter.dataModels.CodeSubmission;

@Service
public class CodeSubmissionProducer {

    private final KafkaTemplate<String, CodeSubmission> kafkaTemplate;
    private final String topic = "code-submissions-topic";

    public CodeSubmissionProducer(KafkaTemplate<String, CodeSubmission> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean pushCodeSubmission(CodeSubmission codeSubmission) {
        kafkaTemplate.send(topic, codeSubmission);
        return true;
    }
}
