package com.chatty.domain;

import com.chatty.domain.enumeration.NotificationType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Notification {

    private String content;
    private NotificationType type;
    private Map<String, String> metadata = new HashMap<>();
    private Instant time;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public void addToMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

}
