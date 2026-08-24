package ru.practicum.explore_with_me.collector.util;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class Utils {
    public static <E extends Enum<E>, F extends Enum<F>> F mapEnum(E enum1, Class<F> enum2Class) {
        return Enum.valueOf(enum2Class, enum1.name());
    }

    public static Instant timestampToInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}