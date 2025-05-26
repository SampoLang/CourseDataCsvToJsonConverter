package com.example.SaarniLearningAssignment.dto;

import lombok.*;

import java.time.LocalDate;

public class ResultDTO {
    private String courseName;
    private String email;
    private String status;
    private Integer grade;
    private LocalDate date;

    public ResultDTO(String courseName, String email, String status, Integer grade, LocalDate date) {
        this.courseName = courseName;
        this.email = email;
        this.status = status;
        this.grade = grade;
        this.date = date;
    }

    public ResultDTO() {
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
