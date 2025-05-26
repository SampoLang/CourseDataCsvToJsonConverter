package com.example.SaarniLearningAssignment.dto;

import lombok.*;

import java.util.Map;


public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private Map<String, Integer> courseResults; // completed, failed, inprogress
    private Double gradeAverage; // nullable if no completed

    public UserDTO(String firstName, String lastName, String email, Map<String, Integer> courseResults, Double gradeAverage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.courseResults = courseResults;
        this.gradeAverage = gradeAverage;
    }

    public UserDTO() {
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

    public Map<String, Integer> getCourseResults() {
        return courseResults;
    }

    public void setCourseResults(Map<String, Integer> courseResults) {
        this.courseResults = courseResults;
    }

    public Double getGradeAverage() {
        return gradeAverage;
    }

    public void setGradeAverage(Double gradeAverage) {
        this.gradeAverage = gradeAverage;
    }
}

