package com.example.investigation_case_service.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendResetCode(String email, String code, Duration ttl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Восстановление пароля");
        message.setText(
                "Ваш код для восстановления пароля: " + code +
                        "\n\nКод действителен в течение " + Math.max(ttl.toMinutes(), 1) + " мин." +
                        "\nЕсли вы не запрашивали восстановление, просто проигнорируйте это письмо."
        );
        mailSender.send(message);
    }
}
