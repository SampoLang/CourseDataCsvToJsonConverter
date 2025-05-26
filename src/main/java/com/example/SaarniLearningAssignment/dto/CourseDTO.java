package com.example.SaarniLearningAssignment.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;


public class CourseDTO {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Integer> results; // completed, failed, inprogress
    private Map<Integer, Integer> grades; // 1-5
    private LocalDate firstCompletionDate;
    private LocalDate mostRecentCompletionDate;

    public CourseDTO(String name, LocalDate startDate, LocalDate endDate, Map<String, Integer> results, Map<Integer, Integer> grades, LocalDate firstCompletionDate, LocalDate mostRecentCompletionDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.results = results;
        this.grades = grades;
        this.firstCompletionDate = firstCompletionDate;
        this.mostRecentCompletionDate = mostRecentCompletionDate;
    }

    public CourseDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Map<String, Integer> getResults() {
        return results;
    }

    public void setResults(Map<String, Integer> results) {
        this.results = results;
    }

    public Map<Integer, Integer> getGrades() {
        return grades;
    }

    public void setGrades(Map<Integer, Integer> grades) {
        this.grades = grades;
    }

    public LocalDate getFirstCompletionDate() {
        return firstCompletionDate;
    }

    public void setFirstCompletionDate(LocalDate firstCompletionDate) {
        this.firstCompletionDate = firstCompletionDate;
    }

    public LocalDate getMostRecentCompletionDate() {
        return mostRecentCompletionDate;
    }

    public void setMostRecentCompletionDate(LocalDate mostRecentCompletionDate) {
        this.mostRecentCompletionDate = mostRecentCompletionDate;
    }
}
