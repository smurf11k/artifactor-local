package com.renata.infrastructure.api.impl;

import com.renata.infrastructure.api.EmailSender;
import com.renata.infrastructure.api.exception.EmailException;
import com.renata.infrastructure.api.exception.VerificationException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderImpl implements EmailSender {

    @Value("${mail.smtp.from}")
    private String emailFrom;

    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 1;
    private final Map<String, VerificationData> verificationDataMap = new ConcurrentHashMap<>();
    private final Session session;
    private final Logger logger = LoggerFactory.getLogger(EmailSenderImpl.class);

    private static class VerificationData {
        String code;
        LocalDateTime creationTime;

        VerificationData(String code, LocalDateTime creationTime) {
            this.code = code;
            this.creationTime = creationTime;
        }
    }

    @Autowired
    public EmailSenderImpl(@Qualifier("mailSession") Session session) {
        this.session = session;
        this.session.setDebug(true);
    }

    @Override
    public void initiateVerification(String email) {
        String verificationCode = String.valueOf((int) (Math.random() * 900000 + 100000));
        verificationDataMap.put(email, new VerificationData(verificationCode, LocalDateTime.now()));
        sendVerificationEmail(email, verificationCode);
    }

    @Override
    public void verifyCodeFromInput(String email, Supplier<String> waitForUserInput) {
        VerificationData data = verificationDataMap.get(email);
        if (data == null) {
            throw new VerificationException("No verification request found for this email.");
        }

        String userInputCode = waitForUserInput.get();
        verifyCode(email, userInputCode);
    }

    /**
     * Відправлення верифікаційного листа на пошту користувача
     *
     * @param email пошта
     * @param verificationCode верифікаційний код
     */
    private void sendVerificationEmail(String email, String verificationCode) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setReplyTo(InternetAddress.parse(emailFrom));
            message.setSubject("Код підтвердження");
            message.setText("Ваш код підтвердження: " + verificationCode);
            logger.info("Preparing to send email from {} to {}", emailFrom, email);
            Transport.send(message);
            logger.info("Verification email successfully sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Error sending verification email to {}: {}", email, e.getMessage());
            throw new EmailException("Помилка при відправці електронного листа: " + e.getMessage());
        }
    }

    /**
     * Верифікація коду надісланого на пошту користувача
     *
     * @param email пошта
     * @param inputCode верифікаційний код
     */
    private void verifyCode(String email, String inputCode) {
        VerificationData data = verificationDataMap.get(email);
        if (data == null) {
            throw new VerificationException("No verification data found.");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(data.creationTime, currentTime);

        if (minutesElapsed > VERIFICATION_CODE_EXPIRATION_MINUTES) {
            verificationDataMap.remove(email);
            throw new VerificationException("Час верифікації вийшов. Спробуйте ще раз.");
        }

        if (!inputCode.equals(data.code)) {
            throw new VerificationException("Неправильний код підтвердження.");
        }

        verificationDataMap.remove(email);
    }
}
