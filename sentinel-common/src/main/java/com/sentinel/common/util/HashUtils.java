package com.sentinel.common.util;

import com.sentinel.common.domain.dto.EventDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    /**
     * Calculates the SHA-256 hash for an Event to ensure idempotency.
     * The hash is based on the immutable properties of the event.
     */
    public static String calculateEventHash(EventDTO event) {
        if (event == null)
            return null;

        // We concatenate the core fields that make an event unique
        String rawString = String.format("%s|%s|%s|%s|%d",
                event.getSourceIp(),
                event.getTimestamp() != null ? event.getTimestamp().toString() : "",
                event.getMethod(),
                event.getEndpoint(),
                event.getBytes());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
