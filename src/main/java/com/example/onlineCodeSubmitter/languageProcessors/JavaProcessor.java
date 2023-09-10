package com.example.onlineCodeSubmitter.languageProcessors;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.onlineCodeSubmitter.constants.Constants;
import com.example.onlineCodeSubmitter.interfaces.LanguageProcessor;
import com.example.onlineCodeSubmitter.service.DockerService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.MemoryStatsConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.DockerClient;

@Component
@Qualifier("javaProcessor")
public class JavaProcessor implements LanguageProcessor {

    private final Long MEMORY_LIMIT_BYTES = 512 * 1024 * 1024L; // 512MB in bytes
    private final Integer THRESHOLD_PERCENTAGE = 80;

    @Value("${host.directory.path}")
    String HOST_DIRECTORY_PATH;

    @Value("${container.volume.path}")
    String REMOTE_VOLUME_PATH;

    @Value("${docker.jdk.image}")
    String DOCKER_JDK_IMAGE;

    @Value("${base.path.code.submission}")
    String BASE_PATH_CODESUBMISSION;

    @Value("${base.path.test.cases}")
    String BASE_PATH_TESTCASES;

    @Value("${remote.base.path}")
    String REMOTE_BASE_PATH;

    @Value("${java.image.name}")
    String JAVA_IMAGE_NAME;

    @Value("${remote.junit.jar.path")
    String REMOTE_JUNITJAR_PATH;

    @Value("${remote.junit.hamcrest.path")
    String REMOTE_JUNIT_HAMCREST_PATH;

    @Value("$junit.runner}")
    String JUNIT_RUNNER;

    @Value("${submission.file.name}")
    String SUBMISSION_FILE_NAME;

    @Value("${test.file.name}")
    String TEST_FILE_NAME;

    @Value("${output.file.name}")
    String OUTPUT_FILE_NAME;

    @Value("${time.limit.millis}")
    Integer TIME_LIMIT_MILLIS;

    

    private final DockerClient dockerClient;
    private final DockerService dockerService;

    @Autowired
    public JavaProcessor(DockerClient dockerClient, DockerService dockerService) {
        this.dockerClient = dockerClient;
        this.dockerService = dockerService;
    }


    @Override
    public void buildImage(String dockerFilePath) {
        dockerService.buildDockerImage(dockerFilePath);
    }

    @Override
    public CreateContainerResponse createDockerContainer(String userId, String questionId) {
        try {
            String codeSubmissionPath = BASE_PATH_CODESUBMISSION+"\\"+userId+"\\"+questionId;
            String testCasesPath = BASE_PATH_TESTCASES+"\\"+questionId;
            // Create a Volume for your Java program and test files
            Volume programVolume = new Volume(codeSubmissionPath);
            Volume testVolume = new Volume(testCasesPath);

            // Specify the Bind options for mounting the volumes
            Bind programBind = Bind.parse(codeSubmissionPath+":"+REMOTE_BASE_PATH);
            Bind testBind = Bind.parse(testCasesPath+":"+REMOTE_BASE_PATH);

            ExposedPort tcp01 = ExposedPort.tcp(8080);

            String bashCommand = createBashCommand(SUBMISSION_FILE_NAME, TEST_FILE_NAME);
            CreateContainerCmd containerCmd = dockerClient
                    .createContainerCmd(JAVA_IMAGE_NAME)
                    .withBinds(List.of(programBind, testBind))
                    .withVolumes(List.of(programVolume, testVolume))
                    .withCmd(
                            "bash",
                            "-c",
                            bashCommand
                    )
                    .withMemory(MEMORY_LIMIT_BYTES)
                    .withExposedPorts(tcp01);

            return containerCmd.exec();//.getId();
        } catch(Exception ex) {
            System.out.println("Exception occured while creating container :: "+ex);
        } 
        return null;
    }
 
    @Override
    public String runCodeInDockerContainer(CreateContainerResponse container, String userId, String questionId) throws IOException {
        try {
            String containerId = container.getId();

             // Start the container
            dockerClient.startContainerCmd(containerId).exec();

            boolean memoryLimitExceeded = checkForMemoryLimitExceeded(containerId);
            if(memoryLimitExceeded) {
                return Constants.Statuses.MEMORY_LIMIT_EXCEEDED;
            }
            boolean isRunning = dockerClient.inspectContainerCmd(container.getId())
                                            .exec()
                                            .getState().getRunning();
            if(isRunning) {
                System.out.println("Time Limit Exceeded");
                 // Kill the container since it exceeded the time limit
                dockerClient.killContainerCmd(containerId).exec();
                return Constants.Statuses.TIME_LIMIT_EXCEEDED;
            } else {
                boolean errorOccured = inspectDockerContainerForErrors(containerId);
                if(errorOccured)
                  return Constants.Statuses.RUNTIME_EXCEPTION;
            }

            String hostOutputPath = getOutputPath(HOST_DIRECTORY_PATH, userId, questionId);
            String remoteOutputPath = REMOTE_BASE_PATH+"//"+OUTPUT_FILE_NAME;

            // Copy the output file from the container to the host
            dockerClient.copyArchiveFromContainerCmd(containerId, remoteOutputPath)
                    .withHostPath(hostOutputPath)
                    .exec();

            System.out.println("Output saved to C:\\DockerTest\\output.txt");
            return Constants.SUCCESS;
        } catch(Exception ex) {
            System.out.println("Error occured while executing code :: "+ex);
        } finally {
            dockerClient.close();
        }
        return null;
    }

    private boolean checkForMemoryLimitExceeded(String containerId) throws Exception{
        int time = 0;
        int factor = TIME_LIMIT_MILLIS / 5;
        while(time < TIME_LIMIT_MILLIS) {
            boolean isRunning = dockerClient.inspectContainerCmd(containerId)
                                            .exec()
                                            .getState().getRunning();
            if(!isRunning)
                return false;
            // dockerClient.statsCmd(containerId);
            //DockerStatsCallback statsCallback = new DockerStatsCallback();
            Statistics stats = dockerClient.statsCmd(containerId).exec(null);
            MemoryStatsConfig memoryStats = stats.getMemoryStats();
            long usage = memoryStats.getUsage();
            long limit = memoryStats.getLimit();
            double memPercent = usage / limit * 100;

            if(memPercent > THRESHOLD_PERCENTAGE) {
                return true;
            }

            // Sleep for a while before querying again
            Thread.sleep(factor); 
            time += factor;
        }
        return false;

    }





    private boolean inspectDockerContainerForErrors(String containerId) {
         // Query Docker for container details
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();

        // Get the exit code of the container (0 indicates success)
        int exitCode = containerInfo.getState().getExitCode();

        // Get other container details, such as status, start time, etc.
        String status = containerInfo.getState().getStatus();
        String startTime = containerInfo.getState().getStartedAt();

        System.out.println("Container Exit Code: " + exitCode);
        System.out.println("Container Status: " + status);
        System.out.println("Container Start Time: " + startTime);
        // if exitcode is non 0 than it means there was some error in execution of code
        return exitCode != 0; 
    }

    private String getOutputPath(String hostName, String userId, String questionId) {
        return new StringBuilder().append(hostName).append("/").append(userId).append("/").append(questionId).toString();
    }

    private String createBashCommand(String submissionFileName, String testFileName) {
        return new StringBuilder().append(createCompilationCommand(submissionFileName, testFileName)).append(";")
                .append(createRunCommand()).append(createOutputCommand()).toString();
    }

    private String createCompilationCommand(String submissionFileName, String testFileName) {
        String compilationCommand = new StringBuilder().append("javac - cp")
                            .append("'").append(REMOTE_JUNITJAR_PATH).append("';")
                            .append("'").append(REMOTE_JUNIT_HAMCREST_PATH).append("'")
                            .append(REMOTE_VOLUME_PATH).append("/").append(submissionFileName)
                            .append(REMOTE_VOLUME_PATH).append("/").append(testFileName)
                            .toString();
        return compilationCommand;
    }

    private String createRunCommand() {
        String testFileNameWithoutExtension = TEST_FILE_NAME.replaceAll(".java", "");
        String runCommand = new StringBuilder().append("javac - cp")
                        .append(REMOTE_VOLUME_PATH).append(":")
                        .append("'").append(REMOTE_JUNITJAR_PATH).append("';")
                        .append("'").append(REMOTE_JUNIT_HAMCREST_PATH).append("'")
                        .append(JUNIT_RUNNER)
                        .append(testFileNameWithoutExtension)
                        .toString();
        return runCommand;
    }

    private String createOutputCommand() {
        return new StringBuilder().append(">").append(REMOTE_VOLUME_PATH).append(OUTPUT_FILE_NAME).toString();
    }

    
}

