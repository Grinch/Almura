/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ClientboundPageListingsPacket implements Message {

    public Set<String> pageNames = new LinkedHashSet<>();

    public ClientboundPageListingsPacket() {
    }

    public ClientboundPageListingsPacket(Set<String> pageNames) {
        this.pageNames = pageNames;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.pageNames.add(buf.readString());
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.pageNames.size());
        this.pageNames.forEach(buf::writeString);
    }
}
