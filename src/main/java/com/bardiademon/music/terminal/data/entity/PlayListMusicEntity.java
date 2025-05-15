package com.bardiademon.music.terminal.data.entity;

import com.bardiademon.music.terminal.data.mapper.PlayListMapper;
import com.bardiademon.music.terminal.utils.JsonSerializable;
import io.vertx.core.json.JsonObject;

public final class PlayListMusicEntity extends BaseEntity implements JsonSerializable {

    private MusicEntity music;
    private PlayListMusicEntity playList;

    public MusicEntity getMusic() {
        return music;
    }

    public void setMusic(MusicEntity music) {
        this.music = music;
    }

    public PlayListMusicEntity getPlayList() {
        return playList;
    }

    public void setPlayList(PlayListMusicEntity playList) {
        this.playList = playList;
    }

    @Override
    public JsonObject toJson() {
        return PlayListMapper.toJsonPlayListMusic(this);
    }

    @Override
    public String toString() {
        return toJson().encode();
    }

}
