/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.keybinding.binder;

import com.google.inject.Injector;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public final class KeyBindingEntry {

    private final KeyBinding keybinding;

    KeyBindingEntry(KeyBinding keybinding) {
        this.keybinding = keybinding;
    }

    public void install(final Injector injector) {
        // TODO Cannot inject the ClientRegistry in Forge without more magic. kashike thing.
        ClientRegistry.registerKeyBinding(this.keybinding);
    }

    public KeyBinding getKeybinding() {
        return this.keybinding;
    }
}
