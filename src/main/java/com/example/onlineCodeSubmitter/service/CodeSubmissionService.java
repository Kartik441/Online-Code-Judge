package com.example.onlineCodeSubmitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.onlineCodeSubmitter.constants.Constants;
import com.example.onlineCodeSubmitter.dataModels.CodeSubmission;
import com.example.onlineCodeSubmitter.factory.LanguageProcessorFactory;
import com.example.onlineCodeSubmitter.interfaces.LanguageProcessor;
import com.example.onlineCodeSubmitter.languageProcessors.JavaProcessor;
import com.github.dockerjava.api.command.CreateContainerResponse;

@Service
public class CodeSubmissionService {

    private final FileHandlingService fileHandlingService;
    private final LanguageProcessorFactory languageProcessorFactory;

    @Value("${base.path.code.submission}")
    String BASE_PATH_CODESUBMISSION;

    @Autowired
    public CodeSubmissionService(FileHandlingService fileHandlingService, LanguageProcessorFactory languageProcessorFactory) {
        this.fileHandlingService = fileHandlingService;
        this.languageProcessorFactory = languageProcessorFactory;
    }
 
    public String submitCode(CodeSubmission codeSubmission) {
        try {
            boolean submissionSaved = fileHandlingService.saveSubmissionFile(codeSubmission.getUserId().toString(), codeSubmission.getQuestionId(), codeSubmission.getSubmissionFile(), BASE_PATH_CODESUBMISSION);
            if(!submissionSaved) {
                System.out.println("Failure in saving submission");
            }
            LanguageProcessor processor = languageProcessorFactory.getProcessor(codeSubmission.getLanguage().name());
            CreateContainerResponse containerResponse = processor.createDockerContainer(codeSubmission.getUserId().toString(), codeSubmission.getQuestionId());
            String output = processor.runCodeInDockerContainer(containerResponse, codeSubmission.getUserId().toString(), codeSubmission.getQuestionId());
            if(Constants.SUCCESS.equals(output)) {
                return fileHandlingService.readOutPutFile(codeSubmission.getUserId().toString(), codeSubmission.getQuestionId(), BASE_PATH_CODESUBMISSION);
            }
            return output;
        } catch(Exception ex) {
            System.out.println("Some failure occured "+ ex);
        }
        return "Failure";
    }
    
}
