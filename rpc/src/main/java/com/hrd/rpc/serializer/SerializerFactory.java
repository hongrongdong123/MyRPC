package com.hrd.rpc.serializer;

import com.hrd.rpc.spi.SpiLoader;

/**
 * description:
 */
public class SerializerFactory {

    private static final Serializer DEFAULT_SERIALIZER = new JsonSerializer();

    static {
        SpiLoader.load(Serializer.class);
    }

    public static Serializer getInstance(String serializerName) {
        return SpiLoader.getInstance(serializerName, Serializer.class);
    }
}
