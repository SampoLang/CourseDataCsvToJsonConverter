package com.example.SaarniLearningAssignment.model;

import com.example.SaarniLearningAssignment.service.JsonWriterService;
import lombok.*;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Data model representing a single course attempt by a user.
 *
 * This class is populated by parsing rows from a CSV file via {@link #fromCsv(CSVRecord)}.
 * It represents both raw data and normalized formats needed for processing and output.
 *
 * Fields include:
 * - Student information (first name, last name, email)
 * - Course metadata (name, start and end dates)
 * - Attempt status (inprogress, completed, failed)
 * - Grade (optional, present only for completed courses)
 * - Completion date (optional for inprogress/failed, required for completed)
 *
 * This class also includes a static parser method {@code fromCsv()} which validates
 * the CSV row structure and ensures logical consistency (e.g. completed must have a date).
 */
public class CourseRecord {
    private String firstName;
    private String lastName;
    private String email;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer grade;
    private LocalDate completionDate;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    private static final Logger logger = LoggerFactory.getLogger(CourseRecord.class);


    /**
     * Parses a single CSVRecord (a row from the uploaded .csv file) and maps it to a CourseRecord object.
     *
     * This method:
     * - Extracts values from the CSV by header name
     * - Converts and validates fields (dates, grade, status)
     * - Ensures that required fields are present and logically valid
     * - Allows missing completionDate only for non-completed statuses
     *
     * @param record a row from a parsed CSV file
     * @return a fully initialized CourseRecord object
     * @throws IllegalArgumentException if any required or invalid field is found
     */
    public static CourseRecord fromCsv(CSVRecord record) {
        try {

            // Required textual fields (trimmed to remove leading/trailing whitespace)
            String firstName = record.get("Etunimi").trim();
            String lastName = record.get("Sukunimi").trim();
            String email = record.get("E-mail").trim();
            String courseName = record.get("Kurssin nimi").trim();

            // Required dates: start and end of the course
            LocalDate startDate = LocalDate.parse(record.get("Kurssi alkaa"), formatter);
            LocalDate endDate = LocalDate.parse(record.get("Kurssi päättyy"), formatter);

            // Status field normalized to lowercase for consistent comparison
            String status = record.get("Status").trim().toLowerCase();

            // Grade field: optional (can be empty)
            String gradeStr = record.get("Arvosana").trim();
            Integer grade = gradeStr.isBlank() ? null : Integer.valueOf(gradeStr);

            // Completion date field: optional for non-completed statuses
            String dateStr = record.get("Kurssin suorituspäivämäärä").trim();
            LocalDate completionDate = (dateStr.isEmpty()) ? null : LocalDate.parse(dateStr, formatter);

            // Logical rule: completionDate is required for "completed" records
            boolean isInprogress = "inprogress".equals(status);

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || courseName.isEmpty()
                    || status.isEmpty() || startDate == null || endDate == null || (!isInprogress && completionDate == null)) {
                throw new IllegalArgumentException("Missing required fields");
            }

            return new CourseRecord(firstName, lastName, email, courseName, startDate, endDate, status, grade, completionDate);

        } catch (Exception e) {
            logger.warn("Failed to parse record: {} | Reason: {}", record.toString(), e.getMessage());
            throw new IllegalArgumentException("Invalid CSV row: " + e.getMessage());
        }
    }

    public CourseRecord(String firstName, String lastName, String email, String courseName, LocalDate startDate, LocalDate endDate, String status, Integer grade, LocalDate completionDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.grade = grade;
        this.completionDate = completionDate;
    }

    public CourseRecord() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
}

