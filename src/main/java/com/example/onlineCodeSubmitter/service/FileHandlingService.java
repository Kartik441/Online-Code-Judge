package com.example.onlineCodeSubmitter.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Files;

@Component
public class FileHandlingService {

    @Value("${submission.file.name}")
    String SUBMISSION_FILE_NAME;

    @Value("${outpuy.file.name}")
    String OUTPUT_FILE_NAME;

    public FileHandlingService() {}

    public boolean saveSubmissionFile(String userId, String questionId, MultipartFile file, String basePath) {
        String userFolder = basePath + "/" + userId;
        String questionFolder = userFolder + "/" + questionId;
        String codeSubmissionFileName = SUBMISSION_FILE_NAME;
        try {
            // Create User folder if it doesn't exist
            File userFolderFile = new File(userFolder);
            if (!userFolderFile.exists()) {
                userFolderFile.mkdirs();
            }

            // Create Question folder if it doesn't exist
            File questionFolderFile = new File(questionFolder);
            if (!questionFolderFile.exists()) {
                questionFolderFile.mkdirs();
            }

            // Save the code submission file
            Path codeSubmissionPath = Path.of(questionFolder, codeSubmissionFileName);
            Files.copy(file.getInputStream(), codeSubmissionPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Code submission saved successfully.");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public String readOutPutFile(String userId, String questionId, String basePath) throws IOException {
        String submissionFilePath = basePath + "/" + userId + "/" + questionId + "/"+OUTPUT_FILE_NAME;
        BufferedReader reader = new BufferedReader(new FileReader(submissionFilePath));
        try {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            // 'content' contains the content of the submission file
            System.out.println("Content of output file:");
            System.out.println(content.toString());
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading output file.");
        } finally {
            reader.close();
        }
        return null;
    }
 }
