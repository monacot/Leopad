package com.leopad.notepad.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        try {
            logger.debug("Verifying Firebase ID token");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            logger.info("Successfully verified Firebase token for user: {}", decodedToken.getUid());
            return decodedToken;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to verify Firebase token: {}", e.getMessage());
            throw e;
        }
    }

    public String getUserEmailFromToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = verifyToken(idToken);
        return decodedToken.getEmail();
    }

    public String getUserIdFromToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = verifyToken(idToken);
        return decodedToken.getUid();
    }

    public String getUserNameFromToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = verifyToken(idToken);
        return decodedToken.getName();
    }

    public boolean isTokenValid(String idToken) {
        try {
            verifyToken(idToken);
            return true;
        } catch (FirebaseAuthException e) {
            logger.warn("Invalid Firebase token: {}", e.getMessage());
            return false;
        }
    }
}