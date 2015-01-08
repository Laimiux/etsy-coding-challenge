package com.example.etsysearch.util;

public class Utils {
    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }

        return value;
    }
}
