package com.bookstore.auth.config;

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

/**
 * Initializes Firebase Admin SDK.
 * Reads service account credentials from the path specified in application.yml.
 */
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
                InputStream serviceAccount = getServiceAccountStream();

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully for project: {}", projectId);
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    private InputStream getServiceAccountStream() throws IOException {
        // Check classpath first (for testing), then filesystem
        InputStream stream = getClass().getClassLoader().getResourceAsStream(serviceAccountPath);
        if (stream == null) {
            stream = new FileInputStream(serviceAccountPath);
        }
        return stream;
    }
}
