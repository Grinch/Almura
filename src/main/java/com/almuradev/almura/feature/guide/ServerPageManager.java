/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.almuradev.almura.shared.event.Witness;
import net.kyori.membrane.facet.Activatable;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerPageManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;

    @Inject
    public ServerPageManager(final Game game) {
        this.game = game;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }
}
