package ru.sber.kkd.rules;

import lombok.Builder;
import lombok.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Value
@Builder
public class DiscoveredIssue {
    String ruleCode;
    String ruleTitle;
    String severity;
    String objectType;
    String objectId;
    String goalId;
    String goalTitle;
    String krId;
    String krTitle;
    String unitId;
    String ownerId;
    String ownerName;
    String details;
    String actualValue;
    String expectedValue;
    String recommendation;

    public String getFingerprint() {
        return ruleCode + ":" + objectId;
    }

    public String generateIssueId() {
        String fp = getFingerprint();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(fp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "ISS-" + hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "ISS-" + Integer.toHexString(fp.hashCode()).toUpperCase();
        }
    }
}
