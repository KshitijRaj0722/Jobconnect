package com.jobconnect.service;

import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String toPhoneNumber, String messageBody) {
        System.out.println("[SMS] To: " + toPhoneNumber + " | " + messageBody);
    }
}
