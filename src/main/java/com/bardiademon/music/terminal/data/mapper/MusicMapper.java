package com.bardiademon.music.terminal.data.mapper;

import com.bardiademon.music.terminal.data.entity.MusicEntity;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

public final class MusicMapper {


    private MusicMapper() {
    }

    public static MusicEntity toMusic(JsonObject row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        MusicEntity music = new MusicEntity();
        music.setId(row.getInteger("id"));
        music.setPath(row.getString("path"));
        music.setFavorite(Mapper.getNullableValue("favorite", row, Integer.class, 0) == 1);
        music.setLastPlay(Mapper.getNullableValue("last_play", row, Integer.class, 0) == 1);
        music.setLastPlayAt(Mapper.toLocalDateTime("last_play_at", row));
        music.setCreatedAt(Mapper.toLocalDateTime("created_at", row));
        return music;
    }

    public static List<MusicEntity> toMusics(List<JsonObject> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(MusicMapper::toMusic).toList();
    }

    public static JsonObject toJson(MusicEntity music) {
        if (music == null) {
            return null;
        }
        return new JsonObject()
                .put("id", music.getId())
                .put("patch", music.getPath())
                .put("last_play_at", music.getLastPlayAt() == null ? null : music.getLastPlayAt().toString())
                .put("last_play", music.isLastPlay())
                .put("favorite", music.isFavorite())
                .put("created_at", music.getCreatedAt() == null ? null : music.getCreatedAt().toString())
                ;
    }

    public static List<JsonObject> toJson(List<MusicEntity> music) {
        if (music == null) {
            return Collections.emptyList();
        }
        return music.stream().map(MusicMapper::toJson).toList();
    }


}
