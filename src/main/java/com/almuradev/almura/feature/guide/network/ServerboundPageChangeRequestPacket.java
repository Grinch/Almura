/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.Page;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundPageChangeRequestPacket implements Message {

    public PageChangeType changeType;
    public String id, name, title, content;
    public int index;

    public ServerboundPageChangeRequestPacket() {
    }

    public ServerboundPageChangeRequestPacket(Page page) {
        this.changeType = PageChangeType.MODIFY;
        this.id = page.getId();
        this.index = page.getIndex();
        this.name = page.getName();
        this.title = page.getTitle();
        this.content = page.getContent();
    }

    public ServerboundPageChangeRequestPacket(String id, int index, String name, String title) {
        this.changeType = PageChangeType.ADD;
        this.id = id;
        this.index = index;
        this.name = name;
        this.title = title;
        this.content = "";
    }

    public ServerboundPageChangeRequestPacket(String id) {
        this.changeType = PageChangeType.REMOVE;
        this.id = id;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.changeType = PageChangeType.of(buf.readByte());
        if (this.changeType != null) {
            this.id = buf.readString();
            if (this.changeType != PageChangeType.REMOVE) {
                this.index = buf.readInteger();
                this.name = buf.readString();
                this.title = buf.readString();
                this.content = buf.readString();
            }
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeByte((byte) this.changeType.ordinal());
        buf.writeString(this.id);
        if (this.changeType != PageChangeType.REMOVE) {
            buf.writeInteger(this.index);
            buf.writeString(this.name);
            buf.writeString(this.title);
            buf.writeString(this.content);
        }
    }
}
