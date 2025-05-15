package com.bardiademon.music.terminal.data.entity;

import com.bardiademon.music.terminal.data.mapper.MusicMapper;
import com.bardiademon.music.terminal.utils.JsonSerializable;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public final class MusicEntity extends BaseEntity implements JsonSerializable {

    private String path;
    private LocalDateTime lastPlayAt;
    private boolean lastPlay;
    private boolean favorite;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getLastPlayAt() {
        return lastPlayAt;
    }

    public void setLastPlayAt(LocalDateTime lastPlayAt) {
        this.lastPlayAt = lastPlayAt;
    }

    public boolean isLastPlay() {
        return lastPlay;
    }

    public void setLastPlay(boolean lastPlay) {
        this.lastPlay = lastPlay;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public JsonObject toJson() {
        return MusicMapper.toJson(this);
    }

    @Override
    public String toString() {
        return toJson().encode();
    }

}
