package com.example.onlineCodeSubmitter.dataModels;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.onlineCodeSubmitter.enums.Language;

@Component
public class CodeSubmission {

    private String userName;
    private Long userId;
    private MultipartFile submissionFile;
    private Language language;
    private String questionId;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public MultipartFile getSubmissionFile() {
        return submissionFile;
    }
    public void setSubmissionFile(MultipartFile submissionFile) {
        this.submissionFile = submissionFile;
    }
    public Language getLanguage() {
        return language;
    }
    public void setLanguage(Language language) {
        this.language = language;
    }
    public String getQuestionId() {
        return questionId;
    }
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    
}
