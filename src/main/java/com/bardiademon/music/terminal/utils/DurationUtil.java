package com.bardiademon.music.terminal.utils;

import java.time.Duration;

public class DurationUtil {

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (minutes < 0) minutes = 0;
        if (seconds < 0) seconds = 0;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
