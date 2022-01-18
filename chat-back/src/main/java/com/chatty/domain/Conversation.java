package com.chatty.domain;

import com.chatty.domain.enumeration.ConversationState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;

/**
 * A Conversation.
 */
@Entity
@Table(name = "conversation")
public class Conversation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_state")
    private ConversationState conversationState;

    @ManyToOne
    @JsonIgnoreProperties(value = { "users", "group" }, allowSetters = true)
    private Room room;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Conversation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public Conversation content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ConversationState getConversationState() {
        return this.conversationState;
    }

    public Conversation conversationState(ConversationState conversationState) {
        this.setConversationState(conversationState);
        return this;
    }

    public void setConversationState(ConversationState conversationState) {
        this.conversationState = conversationState;
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Conversation room(Room room) {
        this.setRoom(room);
        return this;
    }

    public User getSender() {
        return this.sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Conversation sender(User user) {
        this.setSender(user);
        return this;
    }

    public User getReceiver() {
        return this.receiver;
    }

    public void setReceiver(User user) {
        this.receiver = user;
    }

    public Conversation receiver(User user) {
        this.setReceiver(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conversation)) {
            return false;
        }
        return id != null && id.equals(((Conversation) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Conversation{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", conversationState='" + getConversationState() + "'" +
            "}";
    }
}
