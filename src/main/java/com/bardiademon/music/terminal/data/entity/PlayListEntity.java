package com.bardiademon.music.terminal.data.entity;

import com.bardiademon.music.terminal.data.mapper.PlayListMapper;
import com.bardiademon.music.terminal.utils.JsonSerializable;
import io.vertx.core.json.JsonObject;

import java.util.List;

public final class PlayListEntity extends BaseEntity implements JsonSerializable {

    private String name;
    private List<PlayListMusicEntity> musicList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlayListMusicEntity> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<PlayListMusicEntity> musicList) {
        this.musicList = musicList;
    }

    @Override
    public JsonObject toJson() {
        return PlayListMapper.toJsonPlayList(this);
    }

    @Override
    public String toString() {
        return toJson().encode();
    }

}
