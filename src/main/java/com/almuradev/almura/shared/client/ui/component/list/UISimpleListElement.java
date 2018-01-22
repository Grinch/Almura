/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.list;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.format.TextColors;

@SideOnly(Side.CLIENT)
public class UISimpleListElement<T> extends UIBackgroundContainer {

    private int elementHeight = 24;
    private boolean isSelected;
    private T tag;

    public UISimpleListElement(MalisisGui gui, UISimpleList parent) {
        this(gui, parent, 24);
    }

    public UISimpleListElement(MalisisGui gui, UISimpleList parent, int height) {
        super(gui);
        this.parent = parent;
        this.setHeight(height);
        this.applyDefaults();
    }

    public final UISimpleListElement<T> setHeight(int elementHeight) {
        this.elementHeight = elementHeight;
        super.setSize(((UISimpleList) this.parent).getContentWidth(), this.elementHeight);
        return this;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    protected UISimpleListElement<T> setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public T getTag() {
        return this.tag;
    }

    public UISimpleListElement<T> setTag(T tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public final UISimpleListElement<T> setSize(int width, int height) {
        this.setHeight(height);
        return this;
    }

    private void applyDefaults() {
        setBorder(TextColors.DARK_GRAY.getColor().getRgb(), 1, 15);
        setColor(TextColors.BLACK.getColor().getRgb());
    }
}
