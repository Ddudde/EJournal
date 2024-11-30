import com.github.javafaker.Faker;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import ru.data.SSE.TypesConnect;
import ru.security.user.Roles;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

class ExamplesTest {

    @Test
    void testEnumSet() {
        Map<Roles, Integer> f = new HashMap<>(Map.of(Roles.HTEACHER, 2, Roles.PARENT, 6));
        System.out.println(f);
        f.put(Roles.TEACHER, 8);
        System.out.println(f.keySet());
    }

    @Test
    void codesTest() {
        System.out.println(HttpStatus.OK.is5xxServerError());
        System.out.println(HttpStatus.OK.is2xxSuccessful());
        System.out.println(HttpStatus.OK.isError());
        System.out.println();
        System.out.println(HttpStatus.CREATED.is2xxSuccessful());
        System.out.println(HttpStatus.CREATED.isError());
        System.out.println();
        System.out.println(HttpStatus.NO_CONTENT.is2xxSuccessful());
        System.out.println(HttpStatus.NO_CONTENT.isError());
        System.out.println();
        System.out.println(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError());
        System.out.println(HttpStatus.INTERNAL_SERVER_ERROR.isError());
        System.out.println();
        System.out.println(HttpStatus.NOT_FOUND.is4xxClientError());
        System.out.println(HttpStatus.NOT_FOUND.isError());
        System.out.println();
        System.out.println(HttpStatus.CONFLICT.is4xxClientError());
        System.out.println(HttpStatus.CONFLICT.isError());
    }

    @Test
    void testOpt() {
        String t = null;
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            String opt = Optional.ofNullable(t)
                .orElseThrow(() -> new BadCredentialsException("Miss"));
        });
    }

    @Test
    void testFaker() {
        Faker fakerRu = new Faker(new Locale("ru"));
        Faker fakerEn = new Faker();

        String fio = fakerRu.name().lastName() + " " + fakerRu.name().firstName().charAt(0) + "." + fakerRu.name().firstName().charAt(0) + ".";
        System.out.println(fio);
        System.out.println(fakerEn.internet().slug());
        System.out.println(fakerEn.bool().bool());
        System.out.println(fakerEn.internet().emailAddress());
        System.out.println(fakerEn.internet().password());
    }

    @Test
    void setTest2() {
        List<Object[]> arr = List.of(new Object[]{"Англ. Яз", "10.06.22", 1L}, new Object[]{"Математика", "10.06.22", 2L}, new Object[]{"Химия", "10.06.22", 4L}, new Object[]{"Математика", "10.06.22", 5L}, new Object[]{"Математика", "11.06.22", 5L}, new Object[]{"Англ. Яз", "12.06.22", 6L});
        System.out.println(arr);
        Map<String, Map<String, List<Long>>> mapM = arr.stream().collect(Collectors.groupingBy(
            obj -> (String) obj[0],
            Collectors.groupingBy(
                obj1 -> (String) obj1[1],
                Collector.of(
                    ArrayList<Long>::new,
                    (list, item) -> list.add((Long) item[2]),
                    (left, right) -> right
                ))));
//        mapM.put("Англ. Яз", null);
        mapM.put("Англ. Яз1", null);
        System.out.println(mapM);
    }

    @Test
    void setTest1() {
        Map<String, List<Long>> map = Map.of("Англ. Яз", List.of(67L), "Математика", List.of(67L, 68L));
        Set<String> arr = new HashSet<>(map.keySet());
        System.out.println(arr.addAll(List.of("dfgg", "fdg5", "Математика")));
        System.out.println(arr);
        System.out.println(map.keySet());
        System.out.println(map);
    }

    @Test
    void randTest() {
        int max = (int) Math.round(Math.random() * 3) + 2;
        System.out.println(max);
    }

    @Test
    void stringTest1() {
        System.out.println(Objects.equals("_news", "_news"));
        System.out.println("234_news".contains("_news"));
    }

    @Test
    void jsonTest6() throws Exception {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        String json = "{\"name\":\"BMW\",\"model\":\"X1\",\"year\":\"2016\",\"colors\":[\"WHITE\",\"BLACK\",\"GRAY\"]}";
        try{
            wrtr.beginObject().name("name").value("BMW")
                .name("sdf").beginObject()
                .name("dfr").value("sdfg").endObject()
                .name("sdf").beginObject()
                .name("dgt").value("dsft").endObject();
        } catch (Exception e) {
            e.printStackTrace();
            wrtr.name("name").value("df1");
        }
        wrtr.endObject();
        System.out.println("dsf" + wrtr.get().getAsJsonObject());
        Assertions.assertEquals("{\"name\":\"BMW\",\"sdf\":{\"dgt\":\"dsft\"}}", wrtr.get().getAsJsonObject().toString());
        wrtr.close();
    }

    @Test
    void jsonTest5() throws Exception {
        JsonTreeWriter wrtr = new JsonTreeWriter();
        String json = "{\"name\":\"BMW\",\"model\":\"X1\",\"year\":\"2016\",\"colors\":[\"WHITE\",\"BLACK\",\"GRAY\"]}";
        try{
            wrtr.beginObject().name("name").value("BMW")
                .name("year").value(2016)
                .name("test1").value(json)
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
        Assertions.assertNotNull(wrtr);
    }

    /**
    Пропускается тест, из-за возможной необходимости перенастройки подключения к гуглу */
    @Test
    @Disabled
    void notifTest1() throws IOException, FirebaseMessagingException {
        initialize();
        List<String> registrationTokens = asList(
            "c_LTPBf7O7LVs63ZKCrFlC:APA01bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA91bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA31bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB",
            "c_LTPBf7O7LVs63ZKCrFlC:APA61bEs2EPiVtS-HAG9YPaxsj9YhOXhxAEcEVAsID1X_G8gUniOc8nLiHsOgIhwjZZfX7RbRnBD3uWxVkct2h4VtbWP4oRAuY2IBZRy3GSf_g8-Jax34UeGZRqg3LO1HjKIbaAdHWiB"
        );
        unsubscribe(registrationTokens, "readers-club");
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
    }

//    private static void subscribe(List<String> registrationTokens, String topic) {
//        try {
//            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
//                    registrationTokens, topic);
//            System.out.println(response.getSuccessCount() + " tokens were subscribed successfully");
//            if (response != null && response.getFailureCount() > 0) {
//                System.out.println("List of tokens that caused failures: " + response.getErrors());
//            }
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }

    void unsubscribe(List<String> registrationTokens, String topic) throws FirebaseMessagingException {
        TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
            registrationTokens, topic);
        System.out.println(response.getSuccessCount() + " tokens were unsubscribed successfully");
        if (response != null && response.getFailureCount() > 0) {
            System.out.println("List of tokens that caused failures: " + response.getErrors());
        }
    }

    @Test
    void listTest() {
        ArrayList<String> test1 = new ArrayList<>(asList("Jan", "March"));
        ArrayList<String> test = new ArrayList<>();
        test1.remove("March");
    }

    @Test
    void setTest(){
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

    /**
     Пропускается тест, из-за возможной необходимости перенастройки подключения к гуглу */
    @Test
    @Disabled
    void notifTestFailurs() throws IOException {
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

    void initialize() throws IOException {
        InputStream config = ExamplesTest.class.getResourceAsStream("e-journalfcm-firebase-auth.json");
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(config))
            .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase application has been initialized");
        }
    }

    @Test
    void jsonTest4() throws Exception {
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
        Assertions.assertNotNull(wrtr);
    }

    @Test
    void jsonTest3() throws IOException {
        String json = "{\"name\":\"BMW\",\"model\":\"X1\",\"year\":\"2016\",\"colors\":[\"WHITE\",\"BLACK\",\"GRAY\"]}";
        JsonReader rdr = new JsonReader(new StringReader(json));
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
        rdr.close();
        Assertions.assertNotNull(rdr);
    }

    @Test
    void jsonTest2(){
        JsonObject data = new JsonObject(),
            data1 = new JsonObject(),
            data2 = new JsonObject();
        data1.addProperty("id", "fgd");
        data.add("d", data1);
        data1.addProperty("id1", "fgd");
        data2.add("d", data1);
        System.out.println(data);
        System.out.println(data2);
        Assertions.assertTrue(data.has("d") && data2.has("d"));
    }

    @Test
    void dateTest() throws ParseException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println(df.parse(df.format(new Date())));
        System.out.println(df.parse("10.03.2023"));
        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        System.out.println(df.format(dateAfter));
        System.out.println(dateAfter.getTime());
        System.out.println(df.parse("09.03.2023").getTime());
        boolean rez = df.parse(df.format(new Date())).getTime() >= df.parse("09.03.2023").getTime();
        System.out.println(rez);
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//        String text = dtf.format( LocalDateTime.now() );
//        System.out.println(dtf.parse("10.03.2023"));
//        System.out.println(LocalDateTime.now().toLocalDate().atStartOfDay().isBefore(LocalDate.parse("09.03.2023", dtf).atStartOfDay()));
//        System.out.println(text);
        Assertions.assertTrue(rez);
    }

    @Test
    void enumsTest(){
//        System.out.println(TypesConnect.SCHTEACHERS == TypesConnect.HTEACHERS);
//        System.out.println(Objects.equals(TypesConnect.SCHTEACHERS.typeL1, TypesConnect.HTEACHERS.typeL1));
//        System.out.println(TypesConnect.TUTOR.typeL1 != null && Objects.equals(TypesConnect.TUTOR.typeL1, TypesConnect.PROFILES.typeL1));
//        System.out.println(TypesConnect.valueOf("hteachers")); error
        System.out.println(TypesConnect.valueOf("HTEACHERS"));
//        System.out.println(TypesConnect.valueOf("SCHTEACHERS"));
        Assertions.assertNotNull(TypesConnect.valueOf("HTEACHERS"));
    }

    @Test
    void getUuidFromJson(){
        JsonObject data = new JsonObject();
        data.addProperty("uuid", "bda04b06-bbe9-46d4-915e-2220890b9535");
        System.out.println(data.get("uuid").getAsString());
        System.out.println(UUID.fromString(data.get("uuid").getAsString()));
        Assertions.assertNotNull(UUID.fromString(data.get("uuid").getAsString()));
    }

    @Test
    void jsonTest1(){
        JsonObject data = new JsonObject();
        data.addProperty("type", "");
        switch (data.get("type").getAsString()){
            default -> {
                System.out.println("Error Type" + data.get("type"));
//                ans.addProperty("error", true);
//                return ans;
            }
        }
        Assertions.assertNotNull(data.get("type").getAsString());
    }

    @Test
    void jsonTest(){
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
        Assertions.assertNotNull(jsonObject.get("role").getAsString());
    }
}
