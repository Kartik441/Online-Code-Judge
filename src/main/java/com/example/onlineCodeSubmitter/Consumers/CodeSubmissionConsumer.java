package com.example.onlineCodeSubmitter.Consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.onlineCodeSubmitter.controller.WebSocketController;
import com.example.onlineCodeSubmitter.dataModels.CodeSubmission;
import com.example.onlineCodeSubmitter.service.CodeSubmissionService;

@Service
public class CodeSubmissionConsumer {

    private final CodeSubmissionService codeSubmissionService;
    private final WebSocketController webSocketController;

    public CodeSubmissionConsumer(CodeSubmissionService codeSubmissionService, WebSocketController webSocketController) {
        this.codeSubmissionService = codeSubmissionService;
        this.webSocketController = webSocketController;
    }

    @KafkaListener(topics = "code-submissions-topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, CodeSubmission> record) {
        CodeSubmission codeSubmission = record.value();
        String output = codeSubmissionService.submitCode(codeSubmission);
        webSocketController.submitCode(output, codeSubmission.getUserId().toString(), codeSubmission.getQuestionId());
    }
}
