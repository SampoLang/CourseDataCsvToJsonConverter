package com.example.SaarniLearningAssignment;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main Spring Boot application entry point.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SaarniLearningAssignmentApplication {

	@Value("${app.input.folder}")
	private String inputFolder;

	@Value("${app.done.folder}")
	private String doneFolder;

	@Value("${app.output.folder}")
	private String outputFolder;

	@Value("${app.error.folder}")
	private String errorFolder;

	public static void main(String[] args) {
		SpringApplication.run(SaarniLearningAssignmentApplication.class, args);
	}

	/**
	 * Ensures that all required folders exist before the app begins processing files.
	 */
	@PostConstruct
	public void ensureFoldersExist() throws IOException {
		Files.createDirectories(Paths.get(inputFolder));
		Files.createDirectories(Paths.get(doneFolder));
		Files.createDirectories(Paths.get(errorFolder));
		Files.createDirectories(Paths.get(outputFolder));
	}
}
