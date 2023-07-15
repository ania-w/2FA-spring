package com.ania.auth.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MailService {

    @Value("${ania.app.email}")
    private String senderEmail;

    @Value("${ania.app.password}")
    private String senderPassword;

    public void sendAuthDataViaEmail(String email, String user2faData) {

        String subject = "Authentication setup";
        String body = "Your authentication data string is: \n\n" + Base64.encodeBase64String(user2faData.getBytes(StandardCharsets.UTF_8));

        sendEmail(email, subject, body);
    }

    private void sendEmail(String recipientEmail, String subject, String body) {

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.student.put.poznan.pl");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            ExecutorService executorService = Executors.newFixedThreadPool(1);

            executorService.submit(() -> {
                try {
                    Transport.send(message);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
