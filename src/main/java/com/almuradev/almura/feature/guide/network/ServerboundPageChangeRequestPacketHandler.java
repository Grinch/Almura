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
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import java.time.Instant;

import javax.inject.Inject;

public final class ServerboundPageChangeRequestPacketHandler implements MessageHandler<ServerboundPageChangeRequestPacket> {

    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerPageManager manager;

    @Inject
    public ServerboundPageChangeRequestPacketHandler(final Game game, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel
            network, final ServerPageManager manager) {
        this.game = game;
        this.network = network;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPageChangeRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && message.changeType != null) {
            final Player player = ((PlayerConnection) connection).getPlayer();

            Page page = this.manager.getPage(message.id).orElse(null);

            if (message.changeType == PageChangeType.ADD) {
                // If the id being sent up is already in the manager, we've got a desync
                if (page != null) {
                    this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager.getAvailablePagesFor(player).keySet()));
                    return;
                } else {
                    if (!player.hasPermission("almura.guide.add")) {
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id, "You do not "
                                + "have permission to add new pages!"));
                        return;
                    }
                    page = new Page(message.id, player.getUniqueId());
                    page.setIndex(message.index);
                    page.setName(message.name);
                    page.setTitle(message.title);
                    page.setContent(message.content);
                    this.manager.addPage(page);
                }
            } else if (message.changeType == PageChangeType.MODIFY || message.changeType == PageChangeType.REMOVE) {
                // Sent up a modify or remove of a page but someone deleted it, we've got a desync
                if (page == null) {
                    this.network.sendTo(player, new ClientboundPageListingsPacket(this.manager.getAvailablePagesFor(player).keySet()));
                    return;
                }

                if (message.changeType == PageChangeType.MODIFY) {
                    if (!player.hasPermission("almura.guide.modify." + message.id)) {
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id, "You do not "
                                + "have permission to add modify this page!"));
                        return;
                    }

                    page.setIndex(message.index);
                    page.setLastModifier(player.getUniqueId());
                    page.setLastModified(Instant.now());
                    page.setName(message.name);
                    page.setTitle(message.title);
                    page.setContent(message.content);
                } else if (message.changeType == PageChangeType.REMOVE) {
                    if (!player.hasPermission("almura.guide.remove." + message.id)) {
                        this.network.sendTo(player, new ClientboundPageChangeResponsePacket(message.changeType, false, message.id, "You do not "
                                + "have permission to remove this page!"));
                        return;
                    }

                    this.manager.deletePage(message.id);
                }
            }

            if (message.changeType != PageChangeType.REMOVE) {
                this.manager.savePage(page);
            }

            // Sync the listings to everyone
            this.game.getServer().getOnlinePlayers().forEach((online) -> this.network.sendTo(online, new ClientboundPageListingsPacket(this.manager
                    .getAvailablePagesFor(online).keySet())));
        }
    }
}