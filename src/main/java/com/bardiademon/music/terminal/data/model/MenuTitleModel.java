package com.bardiademon.music.terminal.data.model;

import java.util.function.Function;

public record MenuTitleModel<T>(String title, Function<T, String> result, Function<String, T> map) {

    public static MenuTitleModel<Void> createVoid(String title, Function<Void, String> result) {
        return new MenuTitleModel<>(title, result, null);
    }

    public static MenuTitleModel<String> createString(String title, Function<String, String> result) {
        return new MenuTitleModel<>(title, result, item -> item);
    }

}
