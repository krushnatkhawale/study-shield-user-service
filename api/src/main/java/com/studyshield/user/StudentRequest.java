package com.studyshield.user;

import jakarta.validation.constraints.NotBlank;

public class StudentRequest {

    @NotBlank(message = "Student name is required")
    private String name;

    private String gender;

    private Integer birthYear;

    private String studentClass;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
}
