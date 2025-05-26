package com.example.SaarniLearningAssignment.service;

import com.example.SaarniLearningAssignment.dto.CourseDTO;
import com.example.SaarniLearningAssignment.dto.ResultDTO;
import com.example.SaarniLearningAssignment.dto.UserDTO;
import com.example.SaarniLearningAssignment.model.CourseRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;import java.util.stream.Collectors;

/**
 * Service for reading, validating, deduplicating, and summarizing CSV course data.
 *
 * Main responsibilities:
 * - Parsing incoming CSV files and converting them to CourseRecord objects
 * - Filtering out invalid or logically inconsistent records
 * - Deduplicating records so only the most recent per user+course is kept
 * - Generating summaries for output (courses, users, results)
 */
@Service
@EnableScheduling
public class CsvProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingService.class);

    /**
     * Reads the CSV stream, parses it into CourseRecord objects, filters invalid entries,
     * and returns a deduplicated list.
     */
    public List<CourseRecord> parseAndFilter(InputStream is) {
        List<CourseRecord> validRecords = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is))) {
            List<CSVRecord> csvRecords = parser.getRecords();
            if (csvRecords.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty or only contains header");
            }

            for (CSVRecord record : csvRecords) {

                try {
                    CourseRecord courseRecord = CourseRecord.fromCsv(record);

                    // Skip if completion date is outside course duration (only if provided)
                    LocalDate completion = courseRecord.getCompletionDate();
                    if (completion != null &&
                            (completion.isBefore(courseRecord.getStartDate()) || completion.isAfter(courseRecord.getEndDate()))) {
                        logger.warn("Skipping record outside course date range: {}", record);
                        continue;
                    }
                    validRecords.add(courseRecord);
                    logger.debug("Accepted record: {} | {} | {}", courseRecord.getEmail(), courseRecord.getCourseName(), courseRecord.getStatus());
                } catch (Exception e) {
                    logger.warn("Skipping invalid row: {} | Reason: {}", record.toString(), e.getMessage());

                }

            }
        } catch (Exception e) {
            logger.error("Failed to parse CSV", e);
            throw new RuntimeException("Failed to parse CSV", e);
        }

        logger.info("Parsed {} valid course records", validRecords.size());
        return RemoveDuplicates(validRecords);
    }
    /**
     * Removes duplicates of the course records by keeping only the latest attempt per user-course combination.
     * Uses email and course name as the identifiers
     * Prefers completed > failed > inprogress when dates are the same.
     */
    private List<CourseRecord> RemoveDuplicates(List<CourseRecord> records) {
        Map<String, CourseRecord> latestByUserCourse = new HashMap<>();
        for (CourseRecord record : records) {
            String key = record.getEmail() + "|" + record.getCourseName();
            CourseRecord existing = latestByUserCourse.get(key);

            if (existing == null) {
                latestByUserCourse.put(key, record);
                logger.debug("Adding new entry: {}", key);
                continue;
            }

            LocalDate newDate = record.getCompletionDate();
            LocalDate oldDate = existing.getCompletionDate();

            boolean newIsLater = newDate != null && (oldDate == null || newDate.isAfter(oldDate));
            boolean sameDate = newDate != null && newDate.equals(oldDate);

            // Replace if newer or same date with higher status priority
            if (newIsLater || (sameDate && statusPriority(record.getStatus()) > statusPriority(existing.getStatus()))) {
                logger.debug("Replacing entry: {} | Old status: {} -> New status: {}", key, existing.getStatus(), record.getStatus());
                latestByUserCourse.put(key, record);
            }
        }
        logger.info("Removed duplicates down to {} records", latestByUserCourse.size());
        return new ArrayList<>(latestByUserCourse.values());
    }

    /**
     * Summarizes course data:
     * - Number of completed/failed/inprogress
     * - Grade distribution
     * - Earliest and latest completion dates
     */
    public List<CourseDTO> summarizeCourses(List<CourseRecord> records) {
        Map<String, List<CourseRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(CourseRecord::getCourseName));

        List<CourseDTO> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            String courseName = entry.getKey();
            List<CourseRecord> courseRecs = entry.getValue();

            CourseRecord sample = courseRecs.get(0);
            Map<String, Integer> results = new HashMap<>();
            results.put("completed", 0);
            results.put("failed", 0);
            results.put("inprogress", 0);
            Map<Integer, Integer> grades = new HashMap<>();

            LocalDate first = null;
            LocalDate last = null;

            for (CourseRecord r : courseRecs) {
                results.merge(r.getStatus(), 1, Integer::sum);
                if ("completed".equals(r.getStatus()) && r.getGrade() != null) {
                    grades.merge(r.getGrade(), 1, Integer::sum);
                    if (first == null || r.getCompletionDate().isBefore(first)) first = r.getCompletionDate();
                    if (last == null || r.getCompletionDate().isAfter(last)) last = r.getCompletionDate();
                }
            }

            result.add(new CourseDTO(
                    courseName,
                    sample.getStartDate(),
                    sample.getEndDate(),
                    results,
                    grades,
                    first,
                    last
            ));
        }
        logger.info("Summarized {} courses", result.size());
        return result;
    }

    /**
     * Summarizes per-user statistics:
     * - Total count of each status
     * - Average grade for completed courses
     */
    public List<UserDTO> summarizeUsers(List<CourseRecord> records) {
        Map<String, List<CourseRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(CourseRecord::getEmail));

        List<UserDTO> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            String email = entry.getKey();
            List<CourseRecord> userRecs = entry.getValue();
            CourseRecord sample = userRecs.get(0);

            Map<String, Integer> courseResults = new HashMap<>();
            List<Integer> completedGrades = new ArrayList<>();

            courseResults.put("completed", 0);
            courseResults.put("failed", 0);
            courseResults.put("inprogress", 0);

            for (CourseRecord r : userRecs) {
                String status = r.getStatus();
                courseResults.merge(r.getStatus(), 1, Integer::sum);
                if ("completed".equals(r.getStatus()) && r.getGrade() != null) {
                    completedGrades.add(r.getGrade());
                }
            }

            Double gradeAvg = completedGrades.isEmpty() ? null : completedGrades.stream().mapToInt(i -> i).average().orElse(0);

            result.add(new UserDTO(
                    sample.getFirstName(),
                    sample.getLastName(),
                    email,
                    courseResults,
                    gradeAvg
            ));
        }
        logger.info("Summarized {} users", result.size());
        return result;
    }

    /**
     * Generates flat result list for each completed or failed attempt.
     */
    public List<ResultDTO> summarizeResults(List<CourseRecord> records) {

        List<ResultDTO> results = records.stream()
                .filter(r -> "completed".equals(r.getStatus()) || "failed".equals(r.getStatus()))
                .map(r -> new ResultDTO(
                        r.getCourseName(),
                        r.getEmail(),
                        r.getStatus(),
                        r.getGrade(),
                        r.getCompletionDate()
                ))
                .collect(Collectors.toList());
        logger.info("Generated {} course result entries", results.size());
        return results;

    }

    /**
     * Maps course status to numeric priority for deduplication purposes priorizing completed courses.
     */
    private int statusPriority(String status) {
        return switch (status) {
            case "completed" -> 3;
            case "failed" -> 2;
            case "inprogress" -> 1;
            default -> 0;
        };
    }
}

