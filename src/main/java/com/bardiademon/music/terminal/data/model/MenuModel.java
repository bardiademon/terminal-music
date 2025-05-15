package com.bardiademon.music.terminal.data.model;

import java.util.List;

public record MenuModel<T>(String name, List<MenuTitleModel<Void>> numberInputTitles, MenuTitleModel<T> inputMessage) {

    public static <T> MenuModel<T> numberInput(String name, List<MenuTitleModel<Void>> numberInputTitles) {
        return new MenuModel<>(name, numberInputTitles, null);
    }

    public static <T> MenuModel<T> inputMessage(String name, MenuTitleModel<T> inputMessage) {
        return new MenuModel<>(name, null, inputMessage);
    }

}
