package ru.mirea.services;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Service public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    private final String from = "noooreplyejournal1@mail.ru";

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHTMLMessage(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRegCode(String to, String code) {
        String text = """
                <h3>Здравствуйте!</h3>
                <div>
                    Для подтверждения был указан ваш адрес электронной почты.<br>
                    Чтобы продолжить введите код: <b>%s</b><br>
                    Если это делали не вы, просто проигнорируйте письмо.<br>
                    Данный код действителен в течении этого дня(по МСК).
                </div>
                <h3>С уважением, портал EJournal.</h3>
            """.formatted(code);
        sendHTMLMessage(to, "Подтверждение регистрации в EJournal", text);
    }

    public void sendRecCode(String to, String code, String title) {
        String text = """
                <h3>Здравствуйте!</h3>
                <div>
                    Недавно был получен запрос на изменение пароля вашей учётной записи.<br>
                    Для изменения пароля введите код: <b>%s</b><br>
                    Если это делали не вы, просто проигнорируйте письмо.<br>
                    Данный код действителен в течении этого дня(по МСК).
                </div>
                <h3>С уважением, портал EJournal.</h3>
            """.formatted(code);
        sendHTMLMessage(to, title, text);
    }
}
