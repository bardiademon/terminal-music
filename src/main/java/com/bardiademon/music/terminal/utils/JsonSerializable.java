package com.bardiademon.music.terminal.utils;

import io.vertx.core.json.JsonObject;

/**
 * @author @bardiademon
 */
public interface JsonSerializable {
    JsonObject toJson();
}
