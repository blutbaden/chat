package com.chatty.web.websocket.dto;

import com.chatty.domain.enumeration.NotificationType;

public class NotificationDTO {

    private String content;
    private NotificationType type;
    private String metadata;

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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
