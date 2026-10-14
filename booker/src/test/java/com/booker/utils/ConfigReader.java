package com.booker.utils;

import java.io.InputStream;
import java.util.Properties;

/** Reads values from src/test/resources/config.properties (configuration management). */
public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
