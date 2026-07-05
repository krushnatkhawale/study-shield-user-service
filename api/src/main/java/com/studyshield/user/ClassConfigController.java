package com.studyshield.user;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ClassConfigController {

    @GetMapping("/classes")
    public ResponseEntity<?> getClassConfig() {
        try {
            InputStream input = new ClassPathResource("class-config.json").getInputStream();
            byte[] data = input.readAllBytes();
            String json = new String(data);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.ok("{\"default\":{\"name\":\"Default\",\"classes\":[]}}");
        }
    }
}
