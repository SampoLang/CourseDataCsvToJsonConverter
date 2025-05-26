package com.example.SaarniLearningAssignment.service;

import com.example.SaarniLearningAssignment.model.CourseRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Watches the input directory for new .csv files on a schedule.
 *
 * For each detected CSV:
 * - It parses and processes the file using CsvProcessingService
 * - Writes the resulting JSON files using JsonWriterService
 * - Moves the file to a "done" folder if successful, or to an "error" folder on failure
 */
@Service
public class FileWatcherService {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    @Value("${app.scheduler.delay}")
    private long schedulerDelay;
    @Value("${app.input.folder}")
    private String inputFolder;

    @Value("${app.done.folder}")
    private String doneFolder;
    @Value("${app.error.folder}")
    private String errorFolder;

    private final CsvProcessingService csvProcessingService;
    private final JsonWriterService jsonWriterService;

    public FileWatcherService(CsvProcessingService csvProcessingService, JsonWriterService jsonWriterService) {
        this.csvProcessingService = csvProcessingService;
        this.jsonWriterService = jsonWriterService;
    }

    /**
     * Runs on a fixed interval defined in application.properties.
     * Scans the input folder for new CSV files and processes each.
     */
    @Scheduled(fixedDelayString = "${app.scheduler.delay}")
    public void checkForCsvFiles() {
        logger.info("Checking folder for new CSV files: {}", inputFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inputFolder), "*.csv")) {
            for (Path path : stream) {
                logger.info("Processing file: {}", path.getFileName());
                try (FileInputStream is = new FileInputStream(path.toFile())) {
                    // Step 1: Parse and filter records
                    List<CourseRecord> records = csvProcessingService.parseAndFilter(is);
                    // Step 2: Write JSON summaries to output folder
                    jsonWriterService.writeOutputs(records, path.getFileName().toString());
                    // Step 3: Move original CSV to "done" folder
                    Files.move(path, Paths.get(doneFolder, path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Processed file: " + path.getFileName());
                    logger.info("Finished processing: {} → Moved to: {}", path.getFileName(), Paths.get(doneFolder));

                } catch (Exception e) {
                    // If anything goes wrong, move file to error folder and log the error
                    logger.error("Failed to process file {}: {}", path.getFileName(), e.getMessage(), e);
                    System.err.println("Failed to process " + path.getFileName() + ": " + e.getMessage());

                    try {
                        Path errorTarget = Paths.get(errorFolder, path.getFileName().toString());
                        Files.createDirectories(Paths.get(errorFolder)); // ensure it exists
                        Files.move(path, errorTarget, StandardCopyOption.REPLACE_EXISTING);
                        logger.info("Moved failed file to error folder: {}", errorTarget);
                    } catch (IOException ioEx) {
                        logger.error("Failed to move {} to error folder: {}", path.getFileName(), ioEx.getMessage(), ioEx);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error reading input folder {}: {}", inputFolder, e.getMessage(), e);
            System.err.println("Failed to scan input folder: " + e.getMessage());
        }
    }
}
