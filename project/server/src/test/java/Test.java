import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mirea.data.SSE.TypesConnect;
import ru.mirea.data.models.Day;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Test {
    public static void main(String[] args) throws ParseException {
        jsonTest2();
    }

    private static void jsonTest2(){
        JsonObject data = new JsonObject(), data1 = new JsonObject(), data2 = new JsonObject();
        data1.addProperty("id", "fgd");
        data.add("d", data1);
        data1.addProperty("id1", "fgd");
        data2.add("d", data1);
        System.out.println(data);
        System.out.println(data2);

//        List<Day> day = new ArrayList<Day>();
//        day.add(0, new Day("ret"));
//        day.add(1, new Day("pet"));
//        day.add(2, new Day("mek"));
//        day.remove(1);
//        System.out.println(day.get(1));
//        for(Day day1 : day){
//            System.out.println(day1);
//        }
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
