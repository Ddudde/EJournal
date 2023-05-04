package ru.mirea.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.Set;

public class SetStringConverter implements AttributeConverter<Set<String>, String> {

    @Autowired
    private Gson g;

    final Type ex = new TypeToken<Set<String>>(){}.getType();

    public String convertToDatabaseColumn(Set<String> map) {
        return g.toJson(map, ex);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        return g.fromJson(dbData, ex);
    }
}
