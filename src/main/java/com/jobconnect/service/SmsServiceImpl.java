package com.jobconnect.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    private boolean isTwilioConfigured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && !accountSid.startsWith("ACxxxxx") && !accountSid.contains("ACxxxxxxxxxxxxxxxx") &&
            authToken != null && !authToken.isEmpty() && !authToken.contains("your_auth_token")) {
            try {
                Twilio.init(accountSid, authToken);
                isTwilioConfigured = true;
                System.out.println("Twilio SMS Service successfully initialized.");
            } catch (Exception e) {
                System.err.println("Failed to initialize Twilio. Falling back to Mock SMS logging: " + e.getMessage());
            }
        } else {
            System.out.println("Twilio credentials not configured. SMS Service is running in Mock Mode (logs only).");
        }
    }

    @Override
    public void sendSms(String toPhoneNumber, String messageBody) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            System.err.println("Cannot send SMS: Recipient phone number is empty.");
            return;
        }

        if (isTwilioConfigured) {
            try {
                Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber(fromPhoneNumber),
                        messageBody
                ).create();
                System.out.println("SMS sent to " + toPhoneNumber + " via Twilio: " + messageBody);
            } catch (Exception e) {
                System.err.println("Error sending SMS via Twilio to " + toPhoneNumber + ": " + e.getMessage());
                logFallback(toPhoneNumber, messageBody);
            }
        } else {
            logFallback(toPhoneNumber, messageBody);
        }
    }

    private void logFallback(String toPhoneNumber, String messageBody) {
        System.out.println("--- [MOCK SMS ALERT] ---");
        System.out.println("To: " + toPhoneNumber);
        System.out.println("From: " + fromPhoneNumber + " (Mock)");
        System.out.println("Message: " + messageBody);
        System.out.println("-------------------------");
    }
}
