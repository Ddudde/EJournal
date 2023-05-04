package ru.mirea.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.List;

public class ListLongConverter implements AttributeConverter<List<Long>, String> {

    @Autowired
    private Gson g;

    final Type ex = new TypeToken<List<Long>>(){}.getType();

    public String convertToDatabaseColumn(List<Long> map) {
        return g.toJson(map, ex);
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        return g.fromJson(dbData, ex);
    }
}
