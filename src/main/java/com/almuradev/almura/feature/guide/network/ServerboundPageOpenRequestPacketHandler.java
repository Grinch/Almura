/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.ServerPageManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import java.util.Map;

import javax.inject.Inject;

public final class ServerboundPageOpenRequestPacketHandler implements MessageHandler<ServerboundPageOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerPageManager manager;

    @Inject
    public ServerboundPageOpenRequestPacketHandler(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network,
            final ServerPageManager manager) {
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPageOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission("almura.guide.page." + "s")) {
                // We do not have permission for a page we requested to open which means a de-sync. Re-send all page names
                final Map<String, Page> pagesToSend = this.manager.getAvailablePagesFor(player);
                this.network.sendTo(player, new ClientboundPageListingsPacket(pagesToSend.keySet()));
            } else {
                this.network.sendTo(player, new ClientboundPageOpenResponsePacket());
            }
        }
    }
}
