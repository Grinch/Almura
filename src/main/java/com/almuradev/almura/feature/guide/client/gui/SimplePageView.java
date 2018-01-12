/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UISimpleButton;
import com.almuradev.almura.shared.client.ui.component.list.UISimpleListElement;
import com.almuradev.almura.shared.client.ui.component.list.UISimpleList;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;

import java.util.stream.Collectors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageView extends SimpleScreen {

    private static final int INNER_PADDING = 2;
    private static final int ADD_COLOR = Color.ofRgb(115, 115, 115).getRgb();

    @Inject
    private static ClientPageManager manager;

    private boolean showRaw = false;
    private UIButton buttonFormat, buttonSave;
    private UISimpleList list;
    private UITextField contentField;

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        guiscreenBackground = true;

        final UIForm form = new UIForm(this, 300, 225, I18n.format("almura.guide.view.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setClipContent(true);

        final int paddedHeight = SimpleScreen.getPaddedHeight(form);

        // UISimpleList Test
        this.list = new UISimpleList(this, 125, paddedHeight - 50);
        this.list.setPosition(0, 0);
        this.list.setPadding(1, 1);
//        this.list.setUnselect(false);
        this.list.register(this);

        // Content text field
        this.contentField = new UITextField(this, "", true);
        this.contentField.setSize(SimpleScreen.getPaddedWidth(form) - this.list.getWidth() - INNER_PADDING, paddedHeight - 17);
        this.contentField.setPosition(SimpleScreen.getPaddedX(this.list, INNER_PADDING), 0);
        this.contentField.setEditable(false);
        this.contentField.getScrollbar().setAutoHide(true);

        form.add(this.list, this.contentField);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.format":
                this.showRaw = !this.showRaw;

                final String currentContent = this.contentField.getText();
                if (showRaw) {
                    // Need to convert the content from sectional -> ampersand
                    this.contentField.setText(Page.asFriendlyText(currentContent));
                } else {
                    // Need to convert the content from ampersand -> sectional
                    this.contentField.setText(Page.asUglyText(currentContent));
                }
                break;
            case "button.save":
                if (manager.getPage() != null) {
                    final Page page = manager.getPage();
                    final String content = this.contentField.getText();
                    page.setContent(content);
                    manager.requestSavePage();
                }
                break;
            case "button.close":
                close();
                break;
        }
    }

    @Subscribe
    public void onElementSelect(UIListContainer.SelectEvent event) {
        if (event.getNewValue() == null) {
            this.contentField.setText("");
        } else {
            final ListElement element = (ListElement) event.getNewValue();
            if (element instanceof PageListElement){
                manager.requestPage(((PageListElement) event.getNewValue()).getTag().getId());
            } else if (element.getName().equalsIgnoreCase("element.add")) {
                new SimplePageCreate(this).display();
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void refreshPageEntries() {
        // Create elements for every page entry available
//        manager.getPageEntries().forEach(entry -> elementList.add(new PageListElement(this, this.list, entry, ItemTypes.BARRIER)));
        this.list.addElements(manager.getPageEntries().stream()
                .map(entry -> new UISimpleListElement<PageListEntry>(this, this.list))
                .collect(Collectors.toList()));

        // Create 'add' list element
//        final ListElement addElement = new ListElement(this, this.list, "+", null, false, false, false, false);
//        addElement.setContentFontOptions(FontOptions.builder().color(ADD_COLOR).scale(2f).build());
//        addElement.setContentPosition(2, 2, Anchor.MIDDLE | Anchor.CENTER);
//        addElement.setName("element.add");

        // Add 'add' element to list
//        elementList.add(addElement);

        // Set list elements to use list of elements
//        this.list.setElements(elementList);

        // Get the first available element and select it if it is a page element
        boolean hasSelected = false;
//        for (ListElement element : (Iterable<ListElement>) this.list.getElements()) {
//            if (!hasSelected) {
//                hasSelected = true;
//                this.list.select(element);
//            }
//            element.updateContent();
//        }
    }

    public void refreshPage() {
        if (manager.getPage() != null) {
            this.contentField.setText(manager.getPage().getContent());
        }
    }

    @SuppressWarnings("unchecked")
    public void selectPage(String id) {
//        for (ListElement currentElement : (Iterable<ListElement>) this.list.getElements()) {
//            if (currentElement instanceof PageListElement) {
//                final PageListElement currentPageElement = (PageListElement) currentElement;
//                if (currentPageElement.getTag().getId().equalsIgnoreCase(id)) {
//                    this.list.select(currentPageElement);
//                }
//            }
//        }
    }

    private boolean hasAnyPermission() {
        return this.hasEditPermission() || this.hasRemovePermission() || this.hasAddPermission();
    }

    private boolean hasEditPermission() {
        return true;
    }

    private boolean hasRemovePermission() {
        return true;
    }

    private boolean hasAddPermission() {
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    protected static class ListElement<T> extends UIBackgroundContainer {

        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();

        private final boolean showHighlight, canSelect, canDelete, hasMeta;
        private final UISimpleButton deleteButton, metaButton;
        private final UILabel contentLabel;
        protected final T tag;
        private UIImage image;
        protected String content;

        private ListElement(MalisisGui gui, UISimpleList parent, String content, T tag, boolean showHighlight, boolean canSelect, boolean canDelete,
                boolean hasMeta) {
            this(gui, parent, content, tag, ItemTypes.AIR, showHighlight, canSelect, canDelete, hasMeta);
        }

        private ListElement(MalisisGui gui, UISimpleList parent, String content, T tag, ItemType itemType, boolean showHighlight, boolean canSelect,
                boolean canDelete, boolean hasMeta) {
            super(gui);

            // Set parent
            setParent(parent);

            // Set tag
            this.tag = tag;

            // Set content
            this.content = content;

            this.showHighlight = showHighlight;
            this.canSelect = canSelect;
            this.canDelete = canDelete;
            this.hasMeta = hasMeta;

            // Create/add content label
            this.contentLabel = new UILabel(gui, this.content);
            this.contentLabel.setFontOptions(FontOptions.builder()
                    .from(this.contentLabel.getFontOptions())
                    .color(TextColors.WHITE.getColor().getRgb())
                    .shadow(true)
                    .build());
            this.contentLabel.setAnchor(Anchor.MIDDLE | Anchor.LEFT);
            this.contentLabel.setPosition(2, 0);
            this.add(this.contentLabel);

            // Create/add image
            if (itemType != ItemTypes.AIR) {
                this.image = new UIImage(gui, (net.minecraft.item.ItemStack) (Object) ItemStack.of(itemType, 1));
                this.image.setPosition(2, 2);
                this.add(this.image);

                // Move label
                this.contentLabel.setPosition(this.image.getWidth() + 4, 0);
            }

            // Create/add 'x' (delete) button
            this.deleteButton = new UISimpleButton(gui, "x");
            this.deleteButton.setFontOptions(new FontOptions.FontOptionsBuilder().from(this.deleteButton.getFontOptions()).color(TextColors.RED
                    .getColor().getRgb()).build());
            this.deleteButton.setAutoSize(false);
            this.deleteButton.setSize(8, 8);
            this.deleteButton.setPosition(2, 0, Anchor.TOP | Anchor.RIGHT);
            this.deleteButton.setTooltip(I18n.format("almura.guide.view.delete"));
            this.deleteButton.register(this);
            this.deleteButton.setName("button.delete");

            // Create/add '?' (meta) button
            this.metaButton = new UISimpleButton(gui, "?");
            this.metaButton.setFontOptions(new FontOptions.FontOptionsBuilder().from(this.metaButton.getFontOptions()).color(TextColors.YELLOW
                    .getColor().getRgb()).build());
            this.metaButton.setAutoSize(false);
            this.metaButton.setSize(8, 6);
            this.metaButton.setPosition(2, 0, Anchor.BOTTOM | Anchor.RIGHT);
            this.metaButton.setTooltip(I18n.format("almura.guide.view.meta"));
            this.metaButton.register(this);
            this.metaButton.setName("button.meta");

            this.add(this.deleteButton, this.metaButton);

            // Set size
            this.setSize(this.getWidth(), 22);

            // Set padding
            this.setPadding(1, 1);

            this.updateContent();
        }

        public T getTag() {
            return this.tag;
        }

        public FontOptions getContentFontOptions() {
            return this.contentLabel.getFontOptions();
        }

        public void setContentFontOptions(FontOptions options) {
            this.contentLabel.setFontOptions(options);
        }

        public int getContentAnchor() {
            return this.contentLabel.getAnchor();
        }

        public void setContentAnchor(int anchor) {
            this.contentLabel.setAnchor(anchor);
        }

        public Vector2i getContentPosition() {
            return new Vector2i(this.contentLabel.getX(), this.contentLabel.getY());
        }

        public void setContentPosition(int x, int y) {
            this.contentLabel.setPosition(x, y);
        }

        public void setContentPosition(int x, int y, int anchor) {
            this.contentLabel.setPosition(x, y, anchor);
        }

        private void updateButtons(boolean isHovered) {
            this.deleteButton.setVisible(isHovered && this.canDelete);
            this.metaButton.setVisible(isHovered && this.hasMeta);
        }

        private void updateContent() {
            final int maxDisplayWidth = ((UISimpleList) parent).getContentWidth() - (this.image == null ? 4 : this.image.getWidth() + 4) - this.metaButton.getWidth();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                final char currentChar = content.charAt(i);
                final String currentChars = builder.toString();

                if (Minecraft.getMinecraft().fontRenderer.getStringWidth(currentChars + currentChar) > maxDisplayWidth) {
                    builder = new StringBuilder(currentChars.substring(0, currentChars.length() - 2) + "...");
                    break;
                }
                builder.append(currentChar);
            }

            if (!builder.toString().equals(content)) {
                this.contentLabel.setText(builder.toString());
                this.contentLabel.setTooltip(content);
            }
        }

        @Override
        public boolean onClick(int mouseX, int mouseY) {
            return canSelect;
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            final UISimpleList simpleParent = (UISimpleList) parent;

            setSize(simpleParent.getContentWidth(), getHeight());

            if (this.parent != null) {
//                if (this == simpleParent.getSelected()) {
//                    renderer.drawRectangle(simpleParent.getLeftPadding(), simpleParent.getTopPadding(), 0, this.width, this.height, INNER_COLOR,
//                            255);
//                }

                final boolean isHovered = isInsideBounds(mouseX, mouseY);
                if (isHovered && this.showHighlight) {
                    renderer.drawRectangle(simpleParent.getLeftPadding(), simpleParent.getTopPadding(), 0, this.width, this.height, INNER_COLOR,
                            100);
                }
                this.updateButtons(isHovered);
            }
        }

        @Subscribe
        public void onUIButtonClickEvent(UIButton.ClickEvent event) {
            switch (event.getComponent().getName().toLowerCase()) {
                case "button.details":
                case "button.remove":
                    if (manager.getPage() != null) {
                        manager.requestRemovePage(manager.getPage().getId());
                    }
                    break;
            }
        }
    }

    private final static class PageListElement extends ListElement<PageListEntry> {

        private PageListElement(MalisisGui gui, UISimpleList parent, PageListEntry entry) {
            super(gui, parent, entry.getName(), entry, true, true, true, true);
        }

        private PageListElement(MalisisGui gui, UISimpleList parent, PageListEntry entry, ItemType itemType) {
            super(gui, parent, entry.getName(), entry, itemType, true, true, true, true);
        }
    }
}
