package ru.mirea.data.converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.List;

public class ListStringConverter implements AttributeConverter<List<String>, String> {

    @Autowired
    private Gson g;

    final Type ex = new TypeToken<List<String>>(){}.getType();

    public String convertToDatabaseColumn(List<String> map) {
        return g.toJson(map, ex);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return g.fromJson(dbData, ex);
    }
}
