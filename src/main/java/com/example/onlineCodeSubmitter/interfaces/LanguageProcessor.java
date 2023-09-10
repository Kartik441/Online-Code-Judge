package com.example.onlineCodeSubmitter.interfaces;

import java.io.IOException;

import com.github.dockerjava.api.command.CreateContainerResponse;

public interface LanguageProcessor {
    
    public void buildImage(String dockerFilePath);

    public CreateContainerResponse createDockerContainer(String userId, String questionId);  

    public String runCodeInDockerContainer(CreateContainerResponse container, String userId, String questionId) throws IOException;

}
