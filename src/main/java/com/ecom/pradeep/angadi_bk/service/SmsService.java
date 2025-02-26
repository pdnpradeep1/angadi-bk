package com.ecom.pradeep.angadi_bk.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendSms(String phoneNumber, String message) {
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
        // Integrate Twilio, Nexmo, or other SMS API here
    }
}