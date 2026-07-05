package com.studyshield.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> addStudent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody StudentRequest request) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new StudentResponse());
            }
            StudentResponse response = studentService.addStudent(sessionId, request);
            log.info("Student '{}' added", response.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new StudentResponse());
        }
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> listStudents(
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest().body(null);
            }
            List<StudentResponse> students = studentService.listStudents(sessionId);
            return ResponseEntity.ok(students);
        } catch (RegistrationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") UUID studentId,
            @RequestBody StudentRequest request) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                        .body(new StudentResponse());
            }
            StudentResponse response = studentService.updateStudent(sessionId, studentId, request);
            return ResponseEntity.ok(response);
        } catch (RegistrationException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new StudentResponse());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new StudentResponse());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") UUID studentId) {
        try {
            UUID sessionId = extractSessionId(authHeader);
            if (sessionId == null) {
                return ResponseEntity.badRequest().build();
            }
            studentService.deleteStudent(sessionId, studentId);
            log.info("Student '{}' deleted", studentId);
            return ResponseEntity.noContent().build();
        } catch (RegistrationException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private UUID extractSessionId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return UUID.fromString(authHeader.substring(7).trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
