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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public final class ServerboundGuideOpenRequestPacketHandler implements MessageHandler<ServerboundGuideOpenRequestPacket> {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerPageManager manager;

    @Inject
    public ServerboundGuideOpenRequestPacketHandler(final @ChannelId(NetworkConfig.CHANNEL)ChannelBinding.IndexedMessageChannel network,
            final ServerPageManager manager) {
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundGuideOpenRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            if (!player.hasPermission("almura.guide.open")) {
                // TODO Tell the player they cannot open Guide!

                return;
            }

            // Open the GUI
            this.network.sendTo(player, new ClientboundGuideOpenResponsePacket());

            final Map<String, Page> pagesToSend = this.manager.getAvailablePagesFor(player);
            if (pagesToSend.size() > 0) {

                // Send the list of pages
                this.network.sendTo(player, new ClientboundPageListingsPacket(pagesToSend.keySet()));

                // Send the initial page
                final Page page = pagesToSend.entrySet().stream().findFirst().get().getValue();
                this.network.sendTo(player, new ClientboundPageOpenResponsePacket(page));
            }
        }
    }
}
