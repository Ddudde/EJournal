package ru.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.controllers.main.SettingsController;
import ru.security.user.CustomToken;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/** RU: сервис для работы с рассылками по электронной почте */
@Getter
@RequiredArgsConstructor
@Service public class EmailService {

    private final JavaMailSender emailSender;

    /** RU: адрес электронной почты с которой ведётся рассылка */
    private final String from = "noooreplyejournal1@mail.ru";

    /** RU: отправляет письмо
     * @param subject заголовок письма */
    private void sendHTMLMessage(String to, String subject, String text) {
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

    /** RU: посылает на указанную почту код подтверждения для регистрации
     * @param to адрес электронной почты
     * @param code код восстановления
     * @see SettingsController#startEmail(SettingsController.DataSettings, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
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

    /** RU: посылает на указанную почту код подтверждения для восстановление/изменение пароля
     * @param to адрес электронной почты
     * @param code код восстановления
     * @param title заголовок письма
     * @see SettingsController#chPass(SettingsController.DataSettings, CustomToken)  Пример использования */
    @SuppressWarnings("JavadocReference")
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
