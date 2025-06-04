package com.running.util;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class StringListJsonConverter extends ListJsonConverter<String> {
    @Override
    protected TypeReference<List<String>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
