package com.running.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.running.model.Brand;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class BrandListJsonConverter extends ListJsonConverter<Brand> {
    @Override
    protected TypeReference<List<Brand>> getTypeReference() {
        return new TypeReference<>() {};
    }
}
