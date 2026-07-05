package com.studyshield.user;

import java.util.UUID;

public class StudentResponse {

    private UUID studentId;
    private UUID accountId;
    private String name;
    private String gender;
    private Integer birthYear;
    private String studentClass;

    public StudentResponse() {}

    public StudentResponse(UUID studentId, UUID accountId, String name,
                           String gender, Integer birthYear, String studentClass) {
        this.studentId = studentId;
        this.accountId = accountId;
        this.name = name;
        this.gender = gender;
        this.birthYear = birthYear;
        this.studentClass = studentClass;
    }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
}
