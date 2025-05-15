package com.bardiademon.music.terminal.utils;

import java.io.File;

public final class Paths {

    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static final String DATA_PATH = ROOT_PATH + File.separator + "data";
    public static final String DB_NAME = "terminal_music";

    private Paths() {
    }

}
