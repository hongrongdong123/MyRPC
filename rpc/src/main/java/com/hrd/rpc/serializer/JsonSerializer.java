package com.hrd.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * description:
 */
public class JsonSerializer implements Serializer{

    private static final ObjectMapper objectMap = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return objectMap.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        return objectMap.readValue(bytes, tClass);

    }
}
