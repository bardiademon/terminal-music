package com.bardiademon.music.terminal.data.mapper;

import com.bardiademon.music.terminal.data.entity.PlayListEntity;
import com.bardiademon.music.terminal.data.entity.PlayListMusicEntity;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

public final class PlayListMapper {

    private PlayListMapper() {
    }


    public static PlayListEntity toPlayList(JsonObject row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        PlayListEntity playList = new PlayListEntity();
        playList.setId(row.getInteger("id"));
        playList.setName(row.getString("name"));
        playList.setCreatedAt(Mapper.toLocalDateTime("created_at", row));
        return playList;
    }

    public static List<PlayListEntity> toPlayLists(List<JsonObject> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(PlayListMapper::toPlayList).toList();
    }

    public static JsonObject toJsonPlayList(PlayListEntity playList) {
        if (playList == null) {
            return null;
        }
        return new JsonObject()
                .put("id", playList.getId())
                .put("name", playList.getName())
                .put("created_at", playList.getCreatedAt().toString());
    }

    public static List<JsonObject> toJsonPlayList(List<PlayListEntity> playList) {
        if (playList == null || playList.isEmpty()) {
            return Collections.emptyList();
        }
        return playList.stream().map(PlayListMapper::toJsonPlayList).toList();
    }

    public static JsonObject toJsonPlayListMusic(PlayListMusicEntity playListMusic) {
        if (playListMusic == null) {
            return null;
        }
        return new JsonObject()
                .put("id", playListMusic.getId())
                .put("music_id", playListMusic.getMusic().getId())
                .put("play_list", playListMusic.getPlayList().getId())
                .put("created_at", playListMusic.getCreatedAt().toString());
    }

    public static List<JsonObject> toJsonPlayListMusic(List<PlayListMusicEntity> playList) {
        if (playList == null || playList.isEmpty()) {
            return Collections.emptyList();
        }
        return playList.stream().map(PlayListMapper::toJsonPlayListMusic).toList();
    }

}
