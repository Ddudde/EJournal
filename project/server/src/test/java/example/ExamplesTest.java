package example;

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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import ru.data.SSE.TypesConnect;
import ru.security.user.Roles;
import ru.services.MainService;

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

@Slf4j
@Disabled
class ExamplesTest {

    @Test
    void testEnumSet() {
        Map<Roles, Integer> f = new HashMap<>(Map.of(Roles.HTEACHER, 2, Roles.PARENT, 6));
        log.trace(f + "");
        f.put(Roles.TEACHER, 8);
        log.trace(f.keySet() + "");
    }

    @Test
    void codesTest() {
        log.info(HttpStatus.OK.is5xxServerError() + "");
        log.trace(HttpStatus.OK.is2xxSuccessful() + "");
        log.trace(HttpStatus.OK.isError() + "");
        log.trace("");
        log.trace(HttpStatus.CREATED.is2xxSuccessful() + "");
        log.trace(HttpStatus.CREATED.isError() + "");
        log.trace("");
        log.trace(HttpStatus.NO_CONTENT.is2xxSuccessful() + "");
        log.trace(HttpStatus.NO_CONTENT.isError() + "");
        log.trace("");
        log.trace(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError() + "");
        log.trace(HttpStatus.INTERNAL_SERVER_ERROR.isError() + "");
        log.trace("");
        log.trace(HttpStatus.NOT_FOUND.is4xxClientError() + "");
        log.trace(HttpStatus.NOT_FOUND.isError() + "");
        log.trace("");
        log.trace(HttpStatus.CONFLICT.is4xxClientError() + "");
        log.trace(HttpStatus.CONFLICT.isError() + "");
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
        log.trace(fio);
        log.trace(MainService.getRandomUsername(fakerEn));
        log.trace(fakerEn.bool().bool() + "");
        log.trace(fakerEn.internet().emailAddress());
        log.trace(fakerEn.internet().password());
    }

    @Test
    void setTest2() {
        List<Object[]> arr = List.of(new Object[]{"Англ. Яз", "10.06.22", 1L}, new Object[]{"Математика", "10.06.22", 2L}, new Object[]{"Химия", "10.06.22", 4L}, new Object[]{"Математика", "10.06.22", 5L}, new Object[]{"Математика", "11.06.22", 5L}, new Object[]{"Англ. Яз", "12.06.22", 6L});
        log.trace(arr + "");
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
        log.trace(mapM + "");
    }

    @Test
    void setTest1() {
        Map<String, List<Long>> map = Map.of("Англ. Яз", List.of(67L), "Математика", List.of(67L, 68L));
        Set<String> arr = new HashSet<>(map.keySet());
        log.trace(arr.addAll(List.of("dfgg", "fdg5", "Математика")) + "");
        log.trace(arr + "");
        log.trace(map.keySet() + "");
        log.trace(map + "");
    }

    @Test
    void randTest() {
        int max = (int) Math.round(Math.random() * 3) + 2;
        log.trace(max + "");
    }

    @Test
    void stringTest1() {
        log.trace(Objects.equals("_news", "_news") + "");
        log.trace("234_news".contains("_news") + "");
        log.trace("234_news".length() + "");
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
        log.trace("dsf" + wrtr.get().getAsJsonObject());
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
            log.trace(e.fillInStackTrace() + "");
        } finally {
            wrtr.endObject();
            log.trace("dsf" + wrtr.get().getAsJsonObject());
            log.trace("dsf" + wrtr.get().getAsJsonObject().toString());
            log.trace("dsf" + wrtr.get().getAsJsonObject().get("year").toString());
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
        log.trace("Successfully sent message: ");
    }

//    private static void subscribe(List<String> registrationTokens, String topic) {
//        try {
//            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
//                    registrationTokens, topic);
//            log.trace(response.getSuccessCount() + " tokens were subscribed successfully");
//            if (response != null && response.getFailureCount() > 0) {
//                log.trace("List of tokens that caused failures: " + response.getErrors());
//            }
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }

    void unsubscribe(List<String> registrationTokens, String topic) throws FirebaseMessagingException {
        TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                registrationTokens, topic);
        log.trace(response.getSuccessCount() + " tokens were unsubscribed successfully");
        if (response != null && response.getFailureCount() > 0) {
            log.trace("List of tokens that caused failures: " + response.getErrors());
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
        log.trace(stringSet + "");
        stringSet.add("April");
        log.trace(stringSet + "");
        stringSet.remove("April");
        log.trace(stringSet + "");
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
            log.trace("Successfully sent message: ");
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

            log.trace("List of tokens that caused failures: " + failedTokens);
        }
    }

    void initialize() throws IOException {
        InputStream config = ExamplesTest.class.getResourceAsStream("e-journalfcm-firebase-auth.json");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(config))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            log.trace("Firebase application has been initialized");
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
            log.trace(e.fillInStackTrace() + "");
        } finally {
            wrtr.endObject();
            log.trace("dsf" + wrtr.get().getAsJsonObject());
            log.trace("dsf" + wrtr.get().getAsJsonObject().toString());
            log.trace("dsf" + wrtr.get().getAsJsonObject().get("year").toString());
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
                    log.trace(rdr.nextString());
                }
                case "colors" -> {
                    rdr.beginArray();
                    while (rdr.hasNext()){
                        log.trace("\t" + rdr.nextString());
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
        log.trace(data + "");
        log.trace(data2 + "");
        Assertions.assertTrue(data.has("d") && data2.has("d"));
    }

    @Test
    void dateTest() throws ParseException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        log.trace(df.parse(df.format(new Date())) + "");
        log.trace(df.parse("10.03.2023") + "");
        Instant after = Instant.now().plus(Duration.ofDays(30));
        Date dateAfter = Date.from(after);
        log.trace(df.format(dateAfter));
        log.trace(dateAfter.getTime() + "");
        log.trace(df.parse("09.03.2023").getTime() + "");
        boolean rez = df.parse(df.format(new Date())).getTime() >= df.parse("09.03.2023").getTime();
        log.trace(rez + "");
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//        String text = dtf.format( LocalDateTime.now() );
//        log.trace(dtf.parse("10.03.2023"));
//        log.trace(LocalDateTime.now().toLocalDate().atStartOfDay().isBefore(LocalDate.parse("09.03.2023", dtf).atStartOfDay()));
//        log.trace(text);
        Assertions.assertTrue(rez);
    }

    @Test
    void enumsTest(){
//        log.trace(TypesConnect.SCHTEACHERS == TypesConnect.HTEACHERS);
//        log.trace(Objects.equals(TypesConnect.SCHTEACHERS.typeL1, TypesConnect.HTEACHERS.typeL1));
//        log.trace(TypesConnect.TUTOR.typeL1 != null && Objects.equals(TypesConnect.TUTOR.typeL1, TypesConnect.PROFILES.typeL1));
//        log.trace(TypesConnect.valueOf("hteachers")); error
        log.trace(TypesConnect.valueOf("HTEACHERS") + "");
//        log.trace(TypesConnect.valueOf("SCHTEACHERS"));
        Assertions.assertNotNull(TypesConnect.valueOf("HTEACHERS"));
    }

    @Test
    void getUuidFromJson(){
        JsonObject data = new JsonObject();
        data.addProperty("uuid", "bda04b06-bbe9-46d4-915e-2220890b9535");
        log.trace(data.get("uuid").getAsString());
        log.trace(UUID.fromString(data.get("uuid").getAsString()) + "");
        Assertions.assertNotNull(UUID.fromString(data.get("uuid").getAsString()));
    }

    @Test
    void jsonTest1(){
        JsonObject data = new JsonObject();
        data.addProperty("type", "");
        switch (data.get("type").getAsString()){
            default -> {
                log.trace("Error Type" + data.get("type"));
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
//        log.trace(map); //John
//        log.trace(map.get(1L)); //John
//        log.trace(g.toJson(map, RoleMap.class));
        JsonObject jsonObject = JsonParser.parseString("{id: 4, role: 1}").getAsJsonObject();
        log.trace(jsonObject.get("role").getAsString());
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
//        mapper.configure(ALLOW_SINGLE_QUOTES, true);
//        MyMap typeRef = new MyMap();
//        MyMap map = mapper.readValue("{0: {YO: 4, group: 1}, 1: {YO: 8, group: 3}}", MyMap.class);
//        log.trace(map);
//        log.trace(map.get(1L));
//        log.trace(mapper
//                .writerWithDefaultPrettyPrinter()
//                .writeValueAsString(map));
//        log.trace(mapper
//                .writeValueAsString(map));
        Assertions.assertNotNull(jsonObject.get("role").getAsString());
    }
}
