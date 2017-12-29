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

public final class ClientboundGuideOpenResponsePacket implements Message {

    // Global permissions
    public boolean canAdd;

    public ClientboundGuideOpenResponsePacket() {}

    public ClientboundGuideOpenResponsePacket(final boolean canAdd) {
        this.canAdd = canAdd;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.canAdd = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.canAdd);
    }
}
