/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.feature.menu.game.SimpleIngameMenu;
import com.almuradev.almura.feature.menu.main.PanoramicMainMenu;
import com.almuradev.shared.event.Witness;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MainMenuFeature implements Witness {

    @SubscribeEvent
    public void guiOpen(final GuiOpenEvent event) {
        final GuiScreen screen = event.getGui();
        if (screen != null) {
            if (screen.getClass().equals(GuiMainMenu.class)) {
                event.setCanceled(true);
                new PanoramicMainMenu(null).display();
            } else if (screen.getClass().equals(GuiIngameMenu.class)) {
                event.setCanceled(true);
                new SimpleIngameMenu().display();
            }
        }
    }
}
