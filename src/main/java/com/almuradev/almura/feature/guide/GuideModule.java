/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.almuradev.almura.feature.guide.client.gui.SimplePageView;
import com.almuradev.almura.feature.guide.network.ClientboundGuideOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundGuideOpenResponsePacketHandler;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacketHandler;
import com.almuradev.almura.feature.guide.network.ClientboundPageOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageOpenResponsePacketHandler;
import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacketHandler;
import com.almuradev.almura.feature.guide.network.ServerboundPageOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageOpenRequestPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public final class GuideModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.command().child(GuideCommands.generateGuideCommand(), "guide");
        this.packet()
                .bind(ServerboundGuideOpenRequestPacket.class, binder -> binder.handler(ServerboundGuideOpenRequestPacketHandler.class, Platform
                        .Type.SERVER))
                .bind(ClientboundGuideOpenResponsePacket.class,
                        binder -> binder.handler(ClientboundGuideOpenResponsePacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundPageOpenRequestPacket.class, binder -> binder.handler(ServerboundPageOpenRequestPacketHandler.class, Platform.Type
                        .SERVER))
                .bind(ClientboundPageOpenResponsePacket.class,
                        binder -> binder.handler(ClientboundPageOpenResponsePacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundPageListingsPacket.class,
                        binder -> binder.handler(ClientboundPageListingsPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(ServerPageManager.class);
        this.requestStaticInjection(GuideCommands.class);
        if (Sponge.getPlatform().getType().isClient()) {
            this.requestStaticInjection(SimplePageView.class);
        }
    }
}