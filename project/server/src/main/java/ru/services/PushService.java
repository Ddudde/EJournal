package ru.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;
import ru.data.DAO.auth.SettingUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/** RU: сервис для работы с push-уведомлениями пользователям
 * <pre>
 * toDo: Доделать
 *  Протестировать работоспособность
 * </pre> */
@Service public class PushService {

    /** RU: обращение к Firebase */
    private FirebaseMessaging firebase;

    public PushService() throws IOException {
        InputStream config = getClass().getResourceAsStream("/e-journalfcm-firebase-auth.json");
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(config))
            .build();
        if (!FirebaseApp.getApps().isEmpty()) return;
        FirebaseApp.initializeApp(options);
        firebase = FirebaseMessaging.getInstance();
        System.out.println("Firebase application has been initialized");
    }

    /** RU: отправка уведомлений пользователям определённой темы */
    public void send(String topic, String title, String text, String imgUrl) {
        try {
            List<Message> messages = asList(
                Message.builder()
                    .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(text)
                        .setImage(imgUrl)
                        .build())
                    .setTopic(topic)
                    .build()
            );
            firebase.sendAll(messages);
            System.out.println("Successfully sent message: ");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    /** RU: отправка уведомлений списку пользователей */
    public void send(List<String> registrationTokens, String title, String text, String imgUrl) {
        BatchResponse response = null;
        try {
            MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(text)
                    .setImage(imgUrl)
                    .build())
                .addAllTokens(registrationTokens)
                .build();
            response = firebase.sendMulticast(message);
            System.out.println("Successfully sent message: ");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        if (response != null && response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(registrationTokens.get(i));
                }
            }
            System.out.println("List of tokens that caused failures: " + failedTokens);
        }
    }

    /** RU: подписывает токен уведомлений пользователя на определённую тему */
    public int subscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = firebase.subscribeToTopic(registrationTokens, topic);
            System.out.println(response.getSuccessCount() + " request were subscribed successfully");
            if (response != null && response.getFailureCount() > 0) {
                System.out.println("List of tokens that caused failures: " + response.getErrors());
            }
            return response.getFailureCount();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** RU: отписывает токен уведомлений пользователя на определённую тему */
    public int unsubscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = firebase.unsubscribeFromTopic(registrationTokens, topic);
            System.out.println(response.getSuccessCount() + " request were unsubscribed successfully");
            if (response != null && response.getFailureCount() > 0) {
                System.out.println("List of tokens that caused failures: " + response.getErrors());
            }
            return response.getFailureCount();
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** RU: добавляет токен уведомлений пользователю */
    public void addToken(SettingUser settingUser, String token) {
        settingUser.getTokens().add(token);
        settingUser.getTopics().forEach((topic) -> {
            if (settingUser.getNotif()
                    && ((topic.contains("News") && settingUser.getNNewNewsYO())
                    || (topic.contains("news") && settingUser.getNNewNewsPor()))) {
                if (subscribe(asList(token), topic) > 0) {
                    settingUser.getTokens().remove(token);
                }
            }
        });
    }

    /** RU: удаляет токен уведомлений пользователя */
    public void remToken(SettingUser settingUser, String token) {
        settingUser.getTokens().remove(token);
        settingUser.getTopics().forEach((topic) -> {
            unsubscribe(asList(token), topic);
        });
    }

    /** RU: добавляет тему пользователю */
    public void addTopic(SettingUser settingUser, String topic) {
        settingUser.getTopics().add(topic);
//        pushService.subscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }

    /** RU: удаляет тему пользователю */
    public void remTopic(SettingUser settingUser, String topic) {
        settingUser.getTopics().remove(topic);
//        pushService.unsubscribe(new ArrayList<>(settingUser.getTokens()), topic);
    }
}
