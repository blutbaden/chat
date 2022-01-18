package com.chatty.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A Room.
 */
@Entity
@Table(name = "room")
public class Room extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "is_activated")
    private Boolean isActivated;

    @Column(name = "allow_image_message")
    private Boolean allowImageMessage;

    @Column(name = "allow_voice_message")
    private Boolean allowVoiceMessage;

    @Column(name = "allow_sticker_message")
    private Boolean allowStickerMessage;

    @ManyToMany
    @JoinTable(name = "rel_room__user", joinColumns = @JoinColumn(name = "room_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "users" }, allowSetters = true)
    private Group group;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Room id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Room name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActivated() {
        return this.isActivated;
    }

    public Room isActivated(Boolean isActivated) {
        this.setIsActivated(isActivated);
        return this;
    }

    public void setIsActivated(Boolean isActivated) {
        this.isActivated = isActivated;
    }

    public Boolean getAllowImageMessage() {
        return this.allowImageMessage;
    }

    public Room allowImageMessage(Boolean allowImageMessage) {
        this.setAllowImageMessage(allowImageMessage);
        return this;
    }

    public void setAllowImageMessage(Boolean allowImageMessage) {
        this.allowImageMessage = allowImageMessage;
    }

    public Boolean getAllowVoiceMessage() {
        return this.allowVoiceMessage;
    }

    public Room allowVoiceMessage(Boolean allowVoiceMessage) {
        this.setAllowVoiceMessage(allowVoiceMessage);
        return this;
    }

    public void setAllowVoiceMessage(Boolean allowVoiceMessage) {
        this.allowVoiceMessage = allowVoiceMessage;
    }

    public Boolean getAllowStickerMessage() {
        return this.allowStickerMessage;
    }

    public Room allowStickerMessage(Boolean allowStickerMessage) {
        this.setAllowStickerMessage(allowStickerMessage);
        return this;
    }

    public void setAllowStickerMessage(Boolean allowStickerMessage) {
        this.allowStickerMessage = allowStickerMessage;
    }

    public Set<User> getUsers() {
        return this.users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Room users(Set<User> users) {
        this.setUsers(users);
        return this;
    }

    public Room addUser(User user) {
        this.users.add(user);
        return this;
    }

    public Room removeUser(User user) {
        this.users.remove(user);
        return this;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Room group(Group group) {
        this.setGroup(group);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room)) {
            return false;
        }
        return id != null && id.equals(((Room) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Room{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", isActivated='" + getIsActivated() + "'" +
            ", allowImageMessage='" + getAllowImageMessage() + "'" +
            ", allowVoiceMessage='" + getAllowVoiceMessage() + "'" +
            ", allowStickerMessage='" + getAllowStickerMessage() + "'" +
            "}";
    }
}
