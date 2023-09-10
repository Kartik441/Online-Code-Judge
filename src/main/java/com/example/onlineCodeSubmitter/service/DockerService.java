package com.example.onlineCodeSubmitter.service;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;

public class DockerService {

    private final DockerClient dockerClient;
    
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public List<Image> getDockerImages(boolean showIntermediateImages) {
		return showIntermediateImages ? dockerClient.listImagesCmd()
					.withShowAll(true)
					.exec(): dockerClient.listImagesCmd().exec();
	}

	public void buildDockerImage(String dockerFilePath) {
		String image = dockerClient.buildImageCmd()
        .withDockerfile(new File(dockerFilePath))
        .withTag("tag_name")
        .exec(new BuildImageResultCallback())
        .awaitImageId();
	}

	public boolean pullDockerImage(String repository) throws InterruptedException {
		return dockerClient.pullImageCmd(repository)
			.withTag("image_tag")
			.exec(new PullImageResultCallback())
			.awaitCompletion(30, TimeUnit.SECONDS);
	}

}
