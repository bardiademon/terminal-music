package com.bardiademon.music.terminal.exception;

public class UniqueException extends Exception {
    public UniqueException(String name) {
        super("Unique " + name);
    }
}
