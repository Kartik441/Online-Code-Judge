package com.example.onlineCodeSubmitter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.onlineCodeSubmitter.dataModels.CodeSubmission;
import com.example.onlineCodeSubmitter.producers.CodeSubmissionProducer;


@RestController
@RequestMapping("/submit")
public class CodeSubmitController {

    private final CodeSubmissionProducer codeSubmissionProducer;

    public CodeSubmitController(CodeSubmissionProducer codeSubmissionProducer) {
        this.codeSubmissionProducer = codeSubmissionProducer;
    }

    @PostMapping("/code")
    public ResponseEntity<Boolean> submitCode(@RequestBody CodeSubmission codeSubmission) {
        long startTime = System.currentTimeMillis();
        try {
            boolean pushed = codeSubmissionProducer.pushCodeSubmission(codeSubmission);
            return new ResponseEntity<>(pushed, HttpStatus.OK);
        } catch(Exception ex) {
            System.out.println("Exception in pushing code to kafka :: "+ex);
        } finally {
            System.out.println("Total time in executing /submit/code :: "+ (System.currentTimeMillis() - startTime)+" ms");
        }
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    
}
