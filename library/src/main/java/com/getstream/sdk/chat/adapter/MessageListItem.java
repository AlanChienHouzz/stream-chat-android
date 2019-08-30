package com.getstream.sdk.chat.adapter;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MessageListItem {
    private MessageListItemAdapter.EntityType type;
    private Message message;
    private List<ChannelUserRead> messageReadBy;
    private List<MessageViewHolderFactory.Position> positions;
    private Date date;
    private MessageViewHolderFactory.Position messagePosition;
    private Boolean messageMine;
    private List<User> users;

    public MessageListItem(Date date) {
        this.type = MessageListItemAdapter.EntityType.DATE_SEPARATOR;
        this.date = date;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(Message message, List<MessageViewHolderFactory.Position> positions, Boolean messageMine) {
        this.type = MessageListItemAdapter.EntityType.MESSAGE;
        this.message = message;
        this.positions = positions;
        this.messageMine = messageMine;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(List<User> users) {
        this.type = MessageListItemAdapter.EntityType.TYPING;
        this.users = users;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageListItem other = (MessageListItem) obj;

        if (other.type != type) {
            return false;
        }

        switch (type) {
            case TYPING:
                return false;
            case MESSAGE:
                return Objects.equals(other.message, message);
            case DATE_SEPARATOR:
                return Objects.equals(other.date, date);
        }

        return false;
    }

    long getStableID(){
        Checksum checksum = new CRC32();
        String plaintext = type.toString() + ":";
        switch (type) {
            case TYPING:
                plaintext += "typing";
                break;
            case MESSAGE:
                plaintext += message.getId();
                break;
            case DATE_SEPARATOR:
                plaintext += date.toString();
                break;
        }
        checksum.update(plaintext.getBytes(), 0, plaintext.getBytes().length);
        return checksum.getValue();
    }

    public boolean isMine() {
        return this.messageMine;
    }

    boolean isTheirs() {
        return !this.messageMine;
    }

    public Message getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public List<User> getUsers() {
        return users;
    }

    public MessageListItemAdapter.EntityType getType() {
        return type;
    }

    public List<MessageViewHolderFactory.Position> getPositions() {
        return positions;
    }

    public List<ChannelUserRead> getMessageReadBy() {
        return messageReadBy;
    }

    public void removeMessageReadBy() {
        this.messageReadBy = new ArrayList<>();
    }

    public void addMessageReadBy(ChannelUserRead r) {
        this.messageReadBy.add(r);
    }
}
