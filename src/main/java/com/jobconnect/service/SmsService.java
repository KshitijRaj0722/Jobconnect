package com.jobconnect.service;

public interface SmsService {
    void sendSms(String toPhoneNumber, String messageBody);
}
