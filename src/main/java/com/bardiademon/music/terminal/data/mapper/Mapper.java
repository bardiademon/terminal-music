package com.bardiademon.music.terminal.data.mapper;

import com.bardiademon.music.terminal.controller.DatabaseConnection;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Mapper {


    private Mapper() {
    }

    public static LocalDateTime toLocalDateTime(final String name, final JsonObject resultSet) {
        return toLocalDateTime(name, resultSet, DatabaseConnection.SQL_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(final String name, final JsonObject row, final DateTimeFormatter dateTimeFormatter) {
        if (row.getValue(name) != null && row.getValue(name) instanceof final String dateTime) {
            try {
                return LocalDateTime.parse(dateTime, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static <T> T getNullableValue(String name, JsonObject row, Class<T> aClass, T def) {
        Object value = row.getValue(name);
        return aClass.isInstance(value) ? aClass.cast(value) : def;
    }

}
