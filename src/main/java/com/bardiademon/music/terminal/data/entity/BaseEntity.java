package com.bardiademon.music.terminal.data.entity;

import com.bardiademon.music.terminal.utils.JsonSerializable;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class BaseEntity implements JsonSerializable {

    protected int id;
    protected LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("created_at", createdAt.toString());
    }

    @Override
    public String toString() {
        return toJson().encode();
    }
}
