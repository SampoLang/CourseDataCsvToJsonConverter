package com.example.SaarniLearningAssignment.service;

import com.example.SaarniLearningAssignment.dto.CourseDTO;
import com.example.SaarniLearningAssignment.dto.ResultDTO;
import com.example.SaarniLearningAssignment.dto.UserDTO;
import com.example.SaarniLearningAssignment.model.CourseRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service responsible for writing parsed and summarized course data into JSON files.
 *
 * It creates a timestamped subfolder under the output directory and stores:
 * - users.json (per-user statistics)
 * - courses.json (per-course statistics)
 * - course_results.json (flattened result list for completed and failed courses)
 */
@Service
public class JsonWriterService {

    private static final Logger logger = LoggerFactory.getLogger(JsonWriterService.class);

    @Value("${app.output.folder}")
    private String outputFolder;

    private final CsvProcessingService csvProcessingService;

    public JsonWriterService(CsvProcessingService csvProcessingService) {
        this.csvProcessingService = csvProcessingService;
    }

    /**
     * Writes the summarized course data into three separate JSON files under a timestamped folder.
     *
     * @param records          List of parsed and validated course records
     * @param originalFileName Name of the source CSV (used in folder naming)
     */
    public void writeOutputs(List<CourseRecord> records, String originalFileName) throws IOException {

        // Setup JSON mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Format timestamp for folder name
        // Create output directory named like: output/input_2025-05-25T12-34-56
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));
        String baseName = originalFileName.replace(".csv", "");
        Path outputDir = Paths.get(outputFolder, baseName + "_" + timestamp);
        Files.createDirectories(outputDir);

        logger.info("Writing JSON outputs to folder: {}", outputDir);

        // Generate summaries
        List<CourseDTO> courses = csvProcessingService.summarizeCourses(records);
        List<UserDTO> users = csvProcessingService.summarizeUsers(records);
        List<ResultDTO> results = csvProcessingService.summarizeResults(records);

        // Write courses.json
        mapper.writeValue(outputDir.resolve("courses.json").toFile(), courses);
        logger.info("courses.json written with {} entries", courses.size());

        // Write users.json
        mapper.writeValue(outputDir.resolve("users.json").toFile(), users);
        logger.info("users.json written with {} entries", users.size());

        // Write course_results.json
        mapper.writeValue(outputDir.resolve("course_results.json").toFile(), results);
        logger.info("course_results.json written with {} entries", results.size());
    }
}
