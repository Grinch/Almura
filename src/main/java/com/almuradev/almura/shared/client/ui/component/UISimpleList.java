/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.format.TextColors;

@SideOnly(Side.CLIENT)
public class UISimpleList<S extends UIContainer<S>> extends UIListContainer<UISimpleList<S>, S> {
    private boolean showBackground;

    public UISimpleList(MalisisGui gui, int width, int height, boolean showBackground) {
        super(gui, width, height);

        this.showBackground = showBackground;
        this.scrollbar = new UISlimScrollbar(gui, self(), UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);
    }

    public UIScrollBar getScrollBar() {
        return this.scrollbar;
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (!this.showBackground) {
            return;
        }

        // Main background
        renderer.drawRectangle(1, 1, 0, this.width - 2, this.height - 2, TextColors.BLACK.getColor().getRgb(), 130);

        // Outline: Top-Left to Bottom-Left
        renderer.drawRectangle(0, 0, 0, 1, this.height, TextColors.WHITE.getColor().getRgb(), 130);

        // Outline: Top-Left to Top-Right
        renderer.drawRectangle(1, 0, 0, this.width - 2, 1, TextColors.WHITE.getColor().getRgb(), 130);

        // Outline: Top-Right to Bottom-Right
        renderer.drawRectangle(this.width - 1, 0, 0, 1, this.height, TextColors.WHITE.getColor().getRgb(), 130);

        // Outline: Bottom-Left to Bottom-Right
        renderer.drawRectangle(1, this.height - 1, 0, this.width - 2, 1, TextColors.WHITE.getColor().getRgb(), 130);
    }

    @Override
    public int getContentWidth() {
        return super.getWidth() - (this.scrollbar.isVisible() ? this.scrollbar.getWidth() + 2 : 0);
    }

    @Override
    public int getElementHeight(UIContainer element) {
        return element.getHeight();
    }

    @Override
    public void drawElementBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, UIContainer element, boolean isHovered) {
    }

    @Override
    public void drawElementForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, UIContainer element, boolean isHovered) {
        element.draw(renderer, mouseX, mouseY, partialTick);
    }

    @Override
    public float getScrollStep() {
        return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.075F);
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
        return this.scrollbar.onScrollWheel(x, y, delta);
    }
}
