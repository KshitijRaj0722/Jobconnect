package com.jobconnect.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account.sid:ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx}")
    private String accountSid;

    @Value("${twilio.auth.token:your_auth_token_here}")
    private String authToken;

    @Value("${twilio.phone.number:+1234567890}")
    private String fromPhoneNumber;

    private boolean isTwilioConfigured = false;

    @PostConstruct
    public void init() {
        if (!accountSid.startsWith("ACxxxxx") && !authToken.contains("your_auth_token")) {
            try {
                Twilio.init(accountSid, authToken);
                isTwilioConfigured = true;
            } catch (Exception e) {
                System.err.println("Twilio init failed, running in mock mode: " + e.getMessage());
            }
        } else {
            System.out.println("Twilio not configured — SMS running in mock mode.");
        }
    }

    @Override
    public void sendSms(String toPhoneNumber, String messageBody) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) return;
        if (isTwilioConfigured) {
            try {
                Message.creator(new PhoneNumber(toPhoneNumber), new PhoneNumber(fromPhoneNumber), messageBody).create();
            } catch (Exception e) {
                System.err.println("SMS failed: " + e.getMessage());
                logMock(toPhoneNumber, messageBody);
            }
        } else {
            logMock(toPhoneNumber, messageBody);
        }
    }

    private void logMock(String to, String msg) {
        System.out.println("[MOCK SMS] To: " + to + " | " + msg);
    }
}
