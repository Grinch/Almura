/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.ClientPageManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPageListingsPacketHandler implements MessageHandler<ClientboundPageListingsPacket> {

    private final ClientPageManager manager;

    @Inject
    public ClientboundPageListingsPacketHandler(final ClientPageManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPageListingsPacket message, RemoteConnection connection, Platform.Type side) {
        this.manager.setPageNames(message.pageNames);

        // TODO Tell GUI to refresh list of page names Grinch
    }
}
