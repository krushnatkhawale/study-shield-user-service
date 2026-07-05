package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SessionRepository sessionRepository;

    public StudentResponse addStudent(UUID sessionId, StudentRequest request) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        UUID studentId = UUID.randomUUID();
        Student student = new Student();
        student.setStudentId(studentId);
        student.setAccountId(session.getAccountId());
        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setBirthYear(request.getBirthYear());
        student.setStudentClass(request.getStudentClass());
        studentRepository.save(student);

        return new StudentResponse(
                studentId, session.getAccountId(),
                request.getName(), request.getGender(),
                request.getBirthYear(), request.getStudentClass()
        );
    }

    public List<StudentResponse> listStudents(UUID sessionId) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        return studentRepository.findByAccountId(session.getAccountId())
                .stream()
                .map(s -> new StudentResponse(
                        s.getStudentId(), s.getAccountId(),
                        s.getName(), s.getGender(),
                        s.getBirthYear(), s.getStudentClass()))
                .collect(Collectors.toList());
    }

    public StudentResponse updateStudent(UUID sessionId, UUID studentId, StudentRequest request) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RegistrationException("Student not found", "STUDENT_NOT_FOUND"));

        if (!student.getAccountId().equals(session.getAccountId())) {
            throw new RegistrationException("Student does not belong to this account", "UNAUTHORIZED_ACCESS");
        }

        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setBirthYear(request.getBirthYear());
        student.setStudentClass(request.getStudentClass());
        studentRepository.save(student);

        return new StudentResponse(
                student.getStudentId(), student.getAccountId(),
                student.getName(), student.getGender(),
                student.getBirthYear(), student.getStudentClass()
        );
    }

    public void deleteStudent(UUID sessionId, UUID studentId) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RegistrationException("Student not found", "STUDENT_NOT_FOUND"));

        if (!student.getAccountId().equals(session.getAccountId())) {
            throw new RegistrationException("Student does not belong to this account", "UNAUTHORIZED_ACCESS");
        }

        studentRepository.delete(student);
    }
}
