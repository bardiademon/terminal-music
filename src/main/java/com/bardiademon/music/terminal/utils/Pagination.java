package com.bardiademon.music.terminal.utils;

public class Pagination {

    private Pagination() {
    }

    public static int next(int offset, int limit, int total) {
        if (total <= 0 || limit > total) {
            return 0;
        }
        int newOffset = offset + limit;
        if (newOffset > total) {
            return offset;
        }
        return newOffset;
    }

    public static int pre(int offset, int limit, int total) {
        if (offset <= 0 || total <= 0) {
            return 0;
        }
        int newOffset = Math.max(offset, limit) - Math.min(offset, limit);
        return Math.max(newOffset, 0);
    }

}
