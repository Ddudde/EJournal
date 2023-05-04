package ru.mirea.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Service public class PushService {

    public PushService() {
        try {
            InputStream config = getClass().getResourceAsStream("/e-journalfcm-firebase-auth.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(config))
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase application has been initialized");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

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
            FirebaseMessaging.getInstance().sendAll(messages);
            System.out.println("Successfully sent message: ");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

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
            response = FirebaseMessaging.getInstance().sendMulticast(message);
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

    public int subscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                    registrationTokens, topic);
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

    public int unsubscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                    registrationTokens, topic);
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
}
