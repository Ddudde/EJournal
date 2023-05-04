import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.school.day.Day;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

public class Test {

    private static final InputStream config = Test.class.getResourceAsStream("e-journalfcm-firebase-auth.json");

    public static void main(String[] args) throws Exception {
        notifTest1();
    }

    private static void notifTest1(){
        initialize();
        List<String> registrationTokens = asList(
            "c_LTPBf7O7LVs63ZKCrFlC:APA01bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA91bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA31bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA61bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB"
        );
        unsubscribe(registrationTokens, "readers-club");
        try {
            List<Message> messages = asList(
                Message.builder()
                    .setNotification(Notification.builder()
                        .setTitle("Price drop")
                        .setBody("2% off all books")
                        .build())
                    .setTopic("readers-club")
                    .build()
            );
            FirebaseMessaging.getInstance().sendAll(messages);
            System.out.println("Successfully sent message: ");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private static void subscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                    registrationTokens, topic);
            System.out.println(response.getSuccessCount() + " tokens were subscribed successfully");
            if (response != null && response.getFailureCount() > 0) {
                System.out.println("List of tokens that caused failures: " + response.getErrors());
            }
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private static void unsubscribe(List<String> registrationTokens, String topic) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                    registrationTokens, topic);
            System.out.println(response.getSuccessCount() + " tokens were unsubscribed successfully");
            if (response != null && response.getFailureCount() > 0) {
                System.out.println("List of tokens that caused failures: " + response.getErrors());
            }
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    private static void listTest() {
        ArrayList<String> test1 = new ArrayList<>(asList("Jan", "March"));
        ArrayList<String> test = new ArrayList<>();
        test1.remove("March");
    }

    private static void setTest(){
        Set<String> stringSet = new HashSet<>();

        // Добавляем несколько элементов в set
        stringSet.add("Jan");
        stringSet.add("Feb");
        stringSet.add("March");
        stringSet.add("April");
        System.out.println(stringSet);
        stringSet.add("April");
        System.out.println(stringSet);
        stringSet.remove("April");
        System.out.println(stringSet);
    }

    private static void notifTest(){
        initialize();
        BatchResponse response = null;
        List<String> registrationTokens = asList(
            "c_LTPBf7O7LVs63ZKCrFlC:APA01bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA91bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB"
        );
        try {
            MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                    .setTitle("Price drop")
                    .setBody("5% off all electronics")
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

    private static void initialize() {
        try {
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

    private static void mapTest(){
        Map<Integer, Day> day = new HashMap<>();
        day.put(2, new Day("ret"));
        day.put(0, new Day("pet"));
        day.put(1, new Day("mek"));
        day.remove(1);
        System.out.println(day);
        System.out.println(day.get(1));
        for(Map.Entry<Integer, Day> entr : day.entrySet()){
            System.out.println(entr.getKey()+" "+entr.getValue());
        }
        day.put(3, new Day("qek"));
        System.out.println(day);
        for(Map.Entry<Integer, Day> entr : day.entrySet()){
            System.out.println(entr.getKey()+" "+entr.getValue());
        }
    }

    private static void jsonTest4() throws Exception {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        try{
            wrtr.beginObject().name("name").value("BMW")
                .name("year").value(2016)
                .name("colors").beginArray().value("WHITE")
                .value("BLACK").value("GRAY").endArray();
        } catch (Exception e) {
            wrtr.name("name").value("df1");
            System.out.println(e.fillInStackTrace());
        } finally {
            wrtr.endObject();
            System.out.println("dsf" + wrtr.get().getAsJsonObject());
            System.out.println("dsf" + wrtr.get().getAsJsonObject().toString());
            System.out.println("dsf" + wrtr.get().getAsJsonObject().get("year").toString());
            wrtr.close();
        }
    }

    private static void jsonTest3(){
        String json = "{\"name\":\"BMW\",\"model\":\"X1\",\"year\":\"2016\",\"colors\":[\"WHITE\",\"BLACK\",\"GRAY\"]}";
        try (JsonReader rdr = new JsonReader(new StringReader(json))){
            rdr.beginObject();
            while (rdr.hasNext()) {
                switch (rdr.nextName()) {
                    case "name", "model", "year" -> {
                        System.out.println(rdr.nextString());
                    }
                    case "colors" -> {
                        rdr.beginArray();
                        while (rdr.hasNext()){
                            System.out.println("\t" + rdr.nextString());
                        }
                        rdr.endArray();
                    }
                    default -> rdr.skipValue();
                }
            }
            rdr.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void jsonTest2(){
        JsonObject data = new JsonObject(), data1 = new JsonObject(), data2 = new JsonObject();
        data1.addProperty("id", "fgd");
        data.add("d", data1);
        data1.addProperty("id1", "fgd");
        data2.add("d", data1);
        System.out.println(data);
        System.out.println(data2);
    }

    private static void dateTest() throws ParseException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println(df.parse(df.format(new Date())));
        System.out.println(df.parse("10.03.2023"));
        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        System.out.println(df.format(dateAfter));
        System.out.println(dateAfter.getTime());
        System.out.println(df.parse("09.03.2023").getTime());
        System.out.println((df.parse(df.format(new Date())).getTime() >= df.parse("09.03.2023").getTime()));
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//        String text = dtf.format( LocalDateTime.now() );
//        System.out.println(dtf.parse("10.03.2023"));
//        System.out.println(LocalDateTime.now().toLocalDate().atStartOfDay().isBefore(LocalDate.parse("09.03.2023", dtf).atStartOfDay()));
//        System.out.println(text);
    }

    private static void enumsTest(){
//        System.out.println(TypesConnect.SCHTEACHERS == TypesConnect.HTEACHERS);
//        System.out.println(Objects.equals(TypesConnect.SCHTEACHERS.typeL1, TypesConnect.HTEACHERS.typeL1));
//        System.out.println(TypesConnect.TUTOR.typeL1 != null && Objects.equals(TypesConnect.TUTOR.typeL1, TypesConnect.PROFILES.typeL1));
//        System.out.println(TypesConnect.valueOf("hteachers")); error
        System.out.println(TypesConnect.valueOf("HTEACHERS"));
//        System.out.println(TypesConnect.valueOf("SCHTEACHERS"));
    }

    private static void getUuidFromJson(){
        JsonObject data = new JsonObject();
        data.addProperty("uuid", "bda04b06-bbe9-46d4-915e-2220890b9535");
        System.out.println(data.get("uuid").getAsString());
        System.out.println(UUID.fromString(data.get("uuid").getAsString()));
        UUID.fromString(data.get("uuid").getAsString());
    }

    private static void jsonTest1(){
        JsonObject data = new JsonObject();
        data.addProperty("type", "");
        switch (data.get("type").getAsString()){
            default -> {
                System.out.println("Error Type" + data.get("type"));
//                ans.addProperty("error", true);
//                return ans;
            }
        }
    }

    private static void jsonTest(){
        Gson g = new Gson();
//        RoleMap map = g.fromJson("{0: {YO: 4, group: 1}, 1: {YO: 8, group: 3}}", RoleMap.class);
//        System.out.println(map); //John
//        System.out.println(map.get(1L)); //John
//        System.out.println(g.toJson(map, RoleMap.class));
        JsonObject jsonObject = JsonParser.parseString("{id: 4, role: 1}").getAsJsonObject();
        System.out.println(jsonObject.get("role").getAsString());
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
//        mapper.configure(ALLOW_SINGLE_QUOTES, true);
//        MyMap typeRef = new MyMap();
//        MyMap map = mapper.readValue("{0: {YO: 4, group: 1}, 1: {YO: 8, group: 3}}", MyMap.class);
//        System.out.println(map);
//        System.out.println(map.get(1L));
//        System.out.println(mapper
//                .writerWithDefaultPrettyPrinter()
//                .writeValueAsString(map));
//        System.out.println(mapper
//                .writeValueAsString(map));
    }
}
