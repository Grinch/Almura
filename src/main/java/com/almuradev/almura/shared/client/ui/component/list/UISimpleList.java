/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.list;

import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class UISimpleList extends UIComponent<UISimpleList> implements IScrollable, IClipable {

    private boolean multiselect;
    private int bottomPadding, leftPadding, rightPadding, topPadding;
    private int elementSpacing = 1;
    private int lastSize;
    private int xOffset, yOffset;
    private List<UISimpleListElement> elements = new LinkedList<>();
    private UIScrollBar scrollbar;

    public UISimpleList(MalisisGui gui) {
        super(gui);

        this.scrollbar = new UISlimScrollbar(gui, this, UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);
    }

    public UISimpleList(MalisisGui gui, int width, int height) {
        this(gui);
        setSize(width, height);
    }

    public UIScrollBar getScrollBar() {
        return this.scrollbar;
    }

    public UISimpleList setScrollBar(UIScrollBar scrollbar) {
        this.scrollbar = scrollbar;
        return this;
    }

    public boolean getMultiselect() {
        return this.multiselect;
    }

    public UISimpleList setMultiselect(boolean multiselect) {
        this.multiselect = multiselect;
        return this;
    }

    public int getElementSpacing() {
        return this.elementSpacing;
    }

    public UISimpleList setElementSpacing(int elementSpacing) {
        this.elementSpacing = elementSpacing;
        return this;
    }

    public List<UISimpleListElement> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public UISimpleList setElements(Collection<UISimpleListElement> collection) {
        this.elements.clear();
        this.addElements(collection);
        return this;
    }

    public UISimpleList addElements(UISimpleListElement... elements) {
        this.addElements(Arrays.asList(elements));
        return this;
    }

    public UISimpleList addElements(Collection<UISimpleListElement> collection) {
        Optional<UISimpleListElement> optLastElement = this.elements.stream().reduce((first, second) -> second);
        for (UISimpleListElement newElement : collection) {
            // Go ahead and add the element if we have nothing to add padding against
            if (!optLastElement.isPresent()) {
                this.elements.add(newElement);
            } else { // If we have something to pad against, add it and adjust for padding
                newElement.setPosition(0, SimpleScreen.getPaddedY(optLastElement.get(), this.getElementSpacing()));
                this.elements.add(newElement);
                optLastElement = Optional.of(newElement);
            }
        }

        fireEvent(new ContentUpdateEvent<>(this));
        return this;
    }

    public UISimpleList selectElements(UISimpleListElement... elements) {
        return this.setElementsSelection(true, elements);
    }

    public UISimpleList deselectElements(UISimpleListElement... elements) {
        return this.setElementsSelection(false, elements);
    }

    private UISimpleList setElementsSelection(boolean select, UISimpleListElement... elements) {
        if (!this.multiselect) {
            Arrays.stream(elements).findFirst().ifPresent(e -> {
                if (this.elements.contains(e)) {
                    e.setSelected(select);
                }
            });
        } else {
            Arrays.stream(elements).forEach(e -> {
                if (this.elements.contains(e)) {
                    e.setSelected(select);
                }
            });
        }

        return this;
    }

    @Override
    public UISimpleList setSize(int width, int height) {
        super.setSize(width, height);
        this.scrollbar.updateScrollbar();
        return this;
    }

    @Override
    public int getContentWidth() {
        if (this.scrollbar != null && this.scrollbar.isVisible()) {
            return getWidth() - this.scrollbar.getWidth() - 2 - (getLeftPadding() + getRightPadding());
        }

        return getWidth() - this.getLeftPadding() - this.getRightPadding();
    }

    @Override
    public int getContentHeight() {
        if (elements == null || elements.size() == 0) {
            return 0;
        }

        int height = 0;
        for (UISimpleListElement element : this.elements) {
            height += element.getHeight() + this.elementSpacing;
        }

        return height - this.getTopPadding() - this.getBottomPadding();
    }

    @Override
    public UIComponent<?> getComponentAt(int x, int y) {
        final UIComponent<?> superComp = super.getComponentAt(x, y);
        if (superComp != null && superComp != this) {
            return superComp;
        }

        if (!isEnabled() || !isVisible()) {
            return null;
        }

        final Set<UIComponent> list = this.elements.stream().map(c -> c.getComponentAt(x, y)).filter(Objects::nonNull).collect(Collectors.toSet());
        if (list.size() == 0) {
            return superComp;
        }

        UIComponent component = superComp;
        for (UIComponent<?> c : list) {
            if (component != null && (component.getZIndex() <= c.getZIndex())) {
                component = c;
            }
        }

        if (this.shouldClipContent() && !this.getClipArea().isInside(x, y)) {
            return null;
        }

        return component != null && component.isEnabled() ? component : superComp;
    }

    @Override
    public final float getOffsetX() {
        return 0f;
    }

    @Override
    public void setOffsetX(float offsetX, int delta) {
        this.xOffset = Math.round((this.getContentWidth() - getWidth() + delta) * offsetX);
    }

    @Override
    public float getOffsetY() {
        if (this.getContentHeight() <= getHeight()) {
            return 0;
        }
        return (float) this.yOffset / (this.getContentHeight() - getHeight());
    }

    @Override
    public void setOffsetY(float offsetY, int delta) {
        this.yOffset = Math.round((this.getContentHeight() - getHeight() + delta) * offsetY);
    }

    @Override
    public float getScrollStep() {
        return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.075F);
    }

    public UISimpleList setPadding(int horizontalPadding, int verticalPadding) {
        return this.setPadding(horizontalPadding, horizontalPadding, verticalPadding, verticalPadding);
    }

    public UISimpleList setPadding(int leftPadding, int rightPadding, int topPadding, int bottomPadding) {
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        return this;
    }

    @Override
    public int getTopPadding() {
        return this.topPadding;
    }

    public UISimpleList setTopPadding(int topPadding) {
        this.topPadding = topPadding;
        return this;
    }

    @Override
    public int getBottomPadding() {
        return this.bottomPadding;
    }

    public UISimpleList setBottomPadding(int bottomPadding) {
        this.bottomPadding = bottomPadding;
        return this;
    }

    @Override
    public int getLeftPadding() {
        return this.leftPadding;
    }

    public UISimpleList setLeftPadding(int leftPadding) {
        this.leftPadding = leftPadding;
        return this;
    }

    @Override
    public int getRightPadding() {
        return this.rightPadding;
    }

    public UISimpleList setRightPadding(int rightPadding) {
        this.rightPadding = rightPadding;
        return this;
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
        return this.scrollbar.onScrollWheel(x, y, delta);
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.lastSize != this.elements.size())
        {
            this.scrollbar.updateScrollbar();
            this.lastSize = this.elements.size();
        }

        super.draw(renderer, mouseX, mouseY, partialTick);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        this.drawElements(renderer, mouseX, mouseY, partialTick);
    }

    private void drawElements(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.elements.isEmpty()) {
            return;
        }

        final int originalY = y;
        y -= this.yOffset;
        for (UISimpleListElement element : this.elements)
        {
            element.drawBackground(renderer, mouseX, mouseY, partialTick);
            element.drawForeground(renderer, mouseX, mouseY, partialTick);
            y += this.elementSpacing;
        }

        y = originalY;
    }

    @Override
    public final ClipArea getClipArea() {
        return new ClipArea(this);
    }

    @Override
    public final void setClipContent(boolean clip) {
        throw new UnsupportedOperationException("This component does not support this modification.");
    }

    @Override
    public final boolean shouldClipContent() {
        return true;
    }
}
