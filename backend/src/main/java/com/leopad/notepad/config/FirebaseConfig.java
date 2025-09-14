package com.leopad.notepad.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-key-b64}")
    private String serviceAccountKeyB64;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (serviceAccountKeyB64 == null || serviceAccountKeyB64.trim().isEmpty()) {
                    logger.warn("Firebase service account key not provided. Firebase authentication will be disabled.");
                    return;
                }
                
                logger.info("Initializing Firebase Admin SDK for authentication only");
                
                // Decode Base64 to get JSON string
                byte[] decodedBytes = Base64.getDecoder().decode(serviceAccountKeyB64);
                String serviceAccountJson = new String(decodedBytes);
                
                logger.debug("Base64 input length: {}", serviceAccountKeyB64.length());
                logger.debug("Decoded JSON length: {}", serviceAccountJson.length());
                logger.debug("First 100 chars of decoded JSON: {}", serviceAccountJson.substring(0, Math.min(100, serviceAccountJson.length())));
                
                if (serviceAccountJson.trim().isEmpty()) {
                    logger.error("Decoded Firebase JSON is empty after Base64 decoding");
                    return;
                }
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes())
                );

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK initialized successfully for authentication");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase Admin SDK: {}. Firebase authentication will be disabled.", e.getMessage());
            logger.debug("Firebase initialization error details: ", e);
        }
    }

    @Bean
    public FirebaseApp firebaseApp() {
        return FirebaseApp.getInstance();
    }
}