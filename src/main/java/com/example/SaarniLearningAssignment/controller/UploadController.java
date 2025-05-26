package com.example.SaarniLearningAssignment.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

/**
 * REST controller for handling file uploads of CSV course data.
 *
 * - Accepts a multipart/form-data POST request with a single .csv file
 * - Stores the uploaded file into the input folder
 * - The actual parsing and processing is handled asynchronously by FileWatcherService
 * - Includes Swagger/OpenAPI annotations for API documentation
 */
@RestController
@RequestMapping("/v1/records")
@Tag(name = "Course Records", description = "Upload course completion CSV files")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${app.input.folder}")
    private String inputFolder;

    /**
     * Endpoint to receive course CSV uploads via REST API.
     * Saves the file in the input folder where it then will be processed to json
     * Expected file format:
     * - A header row with Finnish column names (Etunimi, Sukunimi, etc.)
     * - UTF-8 encoded .csv file
     */
    @Operation(
            summary = "Upload a CSV file with course records",
            description = """
                      The file must be a `.csv` containing the following columns:\n
                      Etunimi,Sukunimi,E-mail,Kurssin nimi,Kurssi alkaa,Kurssi päättyy,Status,Arvosana,Kurssin suorituspäivämäärä
                      """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "File accepted and queued for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(
            content = @Content(
                    mediaType = "multipart/form-data",
                    examples = @ExampleObject(
                            name = "Example CSV",
                            summary = "CSV with one completed course record",
                            value = "Etunimi,Sukunimi,E-mail,Kurssin nimi,Kurssi alkaa,Kurssi päättyy,Status,Arvosana,Kurssin suorituspäivämäärä\n" +
                                    "Elli,Hurlen,elli.hurlen@example.com,Kiertotalous.nyt2,2022-10-18,2020-12-31,completed,1,2020-10-22"
                    )
            )
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadCsv(
            @Parameter(description = "CSV file to upload", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            logger.warn("File upload failed: empty file");
            return ResponseEntity.badRequest().body("Uploaded file is empty.");
        }

        // Clean filename and check extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!originalFilename.toLowerCase().endsWith(".csv")) {
            logger.warn("Rejected file upload: {} is not a .csv", originalFilename);
            return ResponseEntity.badRequest().body("Only CSV files (.csv) are allowed.");
        }

        // Store file in input folder
        try {
            Path targetPath = Paths.get(inputFolder).resolve(originalFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Uploaded CSV saved to: {}", targetPath);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("CSV uploaded successfully and queued for processing.");
        } catch (IOException e) {
            logger.error("Error saving uploaded CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save the uploaded file.");
        }
    }
}