/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageOpenRequestPacket;
import com.almuradev.almura.shared.client.keybinding.binder.KeyBindingEntry;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientPageManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final KeyBinding guideOpenBinding;

    private Set<String> pageNames = new HashSet<>();
    private Page page;

    @Inject
    public ClientPageManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final Set<KeyBindingEntry>
            keybindings) {
        this.network = network;
        this.guideOpenBinding = keybindings.stream().map(KeyBindingEntry::getKeybinding).filter((keyBinding -> keyBinding
                .getKeyDescription().equalsIgnoreCase("key.almura.guide.open"))).findFirst().orElse(null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (guideOpenBinding.isPressed()) {
            this.network.sendToServer(new ServerboundGuideOpenRequestPacket());
        }
    }

    public Set<String> getPageNames() {
        return Collections.unmodifiableSet(this.pageNames);
    }

    public void setPageNames(Set<String> pageNames) {
        this.pageNames.clear();

        this.pageNames.addAll(pageNames);
    }

    @Nullable
    public Page getPage() {
        return this.page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void requestPage(String pageId) {
        checkNotNull(pageId);

        this.network.sendToServer(new ServerboundPageOpenRequestPacket(pageId));
    }
}
