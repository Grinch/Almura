/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundGuideOpenResponsePacketHandler implements MessageHandler<ClientboundGuideOpenResponsePacket> {

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundGuideOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            // TODO Grinch, the packet will now tell you if you can add new pages, adjust the GUI
            new SimplePageView().display();
        }
    }
}
