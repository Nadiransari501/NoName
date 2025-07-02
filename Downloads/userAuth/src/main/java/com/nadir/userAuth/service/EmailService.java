package com.nadir.userAuth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✔ Email Verification
    public void sendVerificationEmail(String to, String token) {
        String subject = "Email Verification";
        String link = "http://localhost:8080/verify?token=" + token;
        String body = "Please click the following link to verify your email: \n" + link;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }

    // ✅ Forgot Password Reset Email
    public void sendResetPasswordEmail(String to, String token) {
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        String body = "Hi,\n\nTo reset your password, click the following link:\n" + resetLink +
                      "\n\nIf you didn't request a password reset, ignore this email.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }
}
