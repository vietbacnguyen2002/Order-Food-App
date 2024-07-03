package com.bac.se.usermanager.services;

import com.bac.se.usermanager.dto.response.MailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long OTP_EXPIRED = 2 * 60;

    public EmailService(JavaMailSender javaMailSender, RedisTemplate<String, String> redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
    }

    public void sendSimpleMessage(MailBody mailBody) throws MessagingException, FileNotFoundException {
//        send text
        String text = mailBody.text();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom("bacnguyense@gmail.com");
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());
        redisTemplate.opsForValue().set(mailBody.to(),
                text.substring(Math.max(text.length() - 6, 0)),
                Duration.of(OTP_EXPIRED, ChronoUnit.SECONDS));
        javaMailSender.send(message);
//        String mailRecipient = mailBody.to();
//        String text = mailBody.text();
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        mimeMessage.setFrom(new InternetAddress("bacnguyense@gmail.com"));
//        mimeMessage.setRecipients(MimeMessage.RecipientType.TO, mailRecipient);
//        mimeMessage.setSubject(mailBody.subject());
//        mimeMessage.setText(text);
//
//        String htmlTemplate = readFile("template.html");
//        htmlTemplate = htmlTemplate.replace("${text}", text);
//        htmlTemplate = htmlTemplate.replace("${email}", mailBody.to());
//        mimeMessage.setContent(htmlTemplate, "text/html;charset=utf-8");
//        javaMailSender.send(mimeMessage);
    }

}
