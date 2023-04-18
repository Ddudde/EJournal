package ru.mirea.data.converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mirea.data.json.Role;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.Map;

public class MapLongConverter implements AttributeConverter<Map<Long, Long>, String> {

    @Autowired
    private Gson g;

    private final Type ex = new TypeToken<Map<Long,Long>>(){}.getType();

    public String convertToDatabaseColumn(Map<Long,Long> map) {
        return g.toJson(map, ex);
    }

    @Override
    public Map<Long,Long> convertToEntityAttribute(String dbData) {
        return g.fromJson(dbData, ex);
    }
}
