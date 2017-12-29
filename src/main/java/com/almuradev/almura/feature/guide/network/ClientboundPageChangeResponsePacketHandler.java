/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundPageChangeResponsePacketHandler implements MessageHandler<ClientboundPageChangeResponsePacket> {

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPageChangeResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            // TODO Grinch, show a dialog
            // TODO Grinch, if the type is ADD then you can select the appropriate page as I sent the id as well
            // TODO Grinch, packet has a "success" so you can determine a different dialog look to show
        }
    }
}
