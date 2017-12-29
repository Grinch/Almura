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

import java.time.Instant;
import java.util.UUID;

public final class ClientboundPageOpenResponsePacket implements Message {

    public Page page;
    public boolean canSave, canViewMeta, canRemove;

    public ClientboundPageOpenResponsePacket() {
    }

    public ClientboundPageOpenResponsePacket(Page page, boolean canSave, boolean canViewMeta, boolean canRemove) {
        this.page = page;
        this.canSave = canSave;
        this.canViewMeta = canViewMeta;
        this.canRemove = canRemove;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        // GUI perms for this page
        this.canSave = buf.readBoolean();
        this.canViewMeta = buf.readBoolean();
        this.canRemove = buf.readBoolean();

        // Page information
        final String id = buf.readString();
        final int index = buf.readInteger();
        final String name = buf.readString();
        final String title = buf.readString();
        final UUID creator = buf.readUniqueId();
        final Instant created = Instant.parse(buf.readString());
        final UUID lastModifier = buf.readUniqueId();
        final Instant lastModified = Instant.parse(buf.readString());
        final String content = buf.readString();

        this.page = new Page(id, creator, created);
        this.page.setIndex(index);
        this.page.setName(name);
        this.page.setTitle(title);
        this.page.setLastModifier(lastModifier);
        this.page.setLastModified(lastModified);
        this.page.setContent(content);
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        // GUI perms for this page
        buf.writeBoolean(this.canSave);
        buf.writeBoolean(this.canViewMeta);
        buf.writeBoolean(this.canRemove);

        // Page information
        buf.writeString(page.getId());
        buf.writeInteger(page.getIndex());
        buf.writeString(page.getName());
        buf.writeString(page.getTitle());
        buf.writeUniqueId(page.getCreator());
        buf.writeString(page.getCreated().toString());
        buf.writeUniqueId(page.getLastModifier());
        buf.writeString(page.getLastModified().toString());
        buf.writeString(Page.asUglyText(page.getContent()));
    }
}
