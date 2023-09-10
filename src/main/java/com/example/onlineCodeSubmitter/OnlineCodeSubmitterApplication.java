package com.example.onlineCodeSubmitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;


@SpringBootApplication
public class OnlineCodeSubmitterApplication {


	DockerClient dockerClient;

	public static void main(String[] args) {
		SpringApplication.run(OnlineCodeSubmitterApplication.class, args);
	}

	@Bean
	DockerClient dockerClient() {
		return DockerClientBuilder.getInstance(
				DefaultDockerClientConfig.createDefaultConfigBuilder()
						.withDockerHost("tcp://localhost:2375")
						.build())
				.build();
	}


}
