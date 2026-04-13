package com.bookstore.order.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(serviceAccountPath);
                if (stream == null) stream = new FileInputStream(serviceAccountPath);
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .setProjectId(projectId).build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized for order-service");
            }
        } catch (IOException e) {
            throw new RuntimeException("Firebase init failed", e);
        }
    }
}
