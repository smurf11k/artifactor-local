package com.renata.application.impl;

import com.renata.application.contract.EmailService;
import com.renata.application.exception.EmailException;
import com.renata.application.exception.VerificationException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mail.smtp.from}")
    private String emailFrom;

    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 1;
    private LocalDateTime codeCreationTime;
    private String currentVerificationCode;
    private String currentEmail;

    private final Session session;
    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(Session session) {
        this.session = session;
    }

    @Override
    public void initiateVerification(String email) {
        currentVerificationCode = String.valueOf((int) (Math.random() * 900000 + 100000));
        currentEmail = email;
        sendVerificationEmail(email);
        codeCreationTime = LocalDateTime.now();
    }

    @Override
    public void verifyCodeFromInput(String email, Supplier<String> waitForUserInput) {
        if (!email.equals(currentEmail)) {
            throw new VerificationException("Email doesn't match the verification request.");
        }

        String userInputCode = waitForUserInput.get();
        verifyCode(userInputCode);
    }

    private void sendVerificationEmail(String email) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Код підтвердження");
            message.setText("Ваш код підтвердження: " + currentVerificationCode);
            Transport.send(message);
            logger.info("Verification email successfully sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Error sending verification email to {}: {}", email, e.getMessage());
            throw new EmailException("Помилка при відправці електронного листа: " + e.getMessage());
        }
    }

    private void verifyCode(String inputCode) {
        LocalDateTime currentTime = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(codeCreationTime, currentTime);

        if (minutesElapsed > VERIFICATION_CODE_EXPIRATION_MINUTES) {
            throw new VerificationException("Час верифікації вийшов. Спробуйте ще раз.");
        }

        if (!inputCode.equals(currentVerificationCode)) {
            throw new VerificationException("Неправильний код підтвердження.");
        }

        resetVerificationData();
    }

    private void resetVerificationData() {
        codeCreationTime = null;
        currentVerificationCode = null;
        currentEmail = null;
    }
}
