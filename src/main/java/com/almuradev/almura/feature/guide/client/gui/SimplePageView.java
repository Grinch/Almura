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
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageView extends SimpleScreen {

    private static final int INNER_PADDING = 2;

    @Inject
    private static ClientPageManager manager;

    private boolean showRaw = false;
    private UIButton buttonRemove, buttonAdd, buttonDetails, buttonFormat;
    private UISelect<PageListEntry> pagesSelect;
    private UISimpleList list;
    private UITextField contentField;

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        guiscreenBackground = true;

        final UIForm form = new UIForm(this, 250, 225, I18n.format("almura.guide.view.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setClipContent(false);

        // Remove button
        this.buttonRemove = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.RED, "-"))
                .tooltip(Text.of("almura.guide.view.remove"))
                .visible(hasAnyPermission())
                .enabled(hasRemovePermission())
                .listener(this)
                .build("button.remove");

        // Details button
        this.buttonDetails = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.YELLOW, "?"))
                .tooltip(Text.of(I18n.format("almura.guide.view.details")))
                .anchor(Anchor.TOP | Anchor.RIGHT)
                .visible(hasAnyPermission())
                .listener(this)
                .build("button.help");

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .width(10)
                .x(SimpleScreen.getPaddedX(this.buttonDetails, 2, Anchor.RIGHT))
                .text(Text.of(TextColors.GREEN, "+"))
                .tooltip(Text.of("almura.guide.view.add"))
                .anchor(Anchor.TOP | Anchor.RIGHT)
                .visible(hasAnyPermission())
                .enabled(hasAddPermission())
                .listener(this)
                .build("button.add");

        // Pages dropdown
        this.pagesSelect = new UISelect<>(this, SimpleScreen.getPaddedWidth(form));
        this.pagesSelect.setLabelFunction(PageListEntry::getName);
        this.pagesSelect.setName("combobox.pages");
        this.pagesSelect.setPosition(this.buttonRemove.isVisible() ? SimpleScreen.getPaddedX(this.buttonRemove, INNER_PADDING) : 0, 0);
        this.pagesSelect.register(this);
        if (hasAnyPermission()) {
            this.pagesSelect.setSize(this.pagesSelect.getWidth() - this.buttonDetails.getWidth() - this.buttonAdd.getWidth() - this.buttonRemove
                    .getWidth() - (INNER_PADDING * 4) + 2, 15);
        }

        // Formatted button
        this.buttonFormat = new UIButtonBuilder(this)
                .width(10)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .visible(this.hasAnyPermission())
                .listener(this)
                .build("button.format");

        // Content text field
        contentField = new UITextField(this, "", true);
        contentField.setSize(SimpleScreen.getPaddedWidth(form),
                SimpleScreen.getPaddedHeight(form) - this.pagesSelect.getHeight() - (INNER_PADDING * 2) - this.buttonFormat.getHeight());
        contentField.setPosition(0, SimpleScreen.getPaddedY(this.pagesSelect, INNER_PADDING));
        contentField.setEditable(true);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        // Save button
        final UIButton buttonSave = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .x(SimpleScreen.getPaddedX(buttonClose, INNER_PADDING, Anchor.RIGHT))
                .text(Text.of("almura.guide.button.save"))
                .visible(hasAnyPermission())
                .enabled(hasEditPermission())
                .listener(this)
                .build("button.save");

//        form.add(this.buttonRemove, this.pagesSelect, this.buttonDetails, this.buttonAdd, this.buttonFormat, contentField, buttonClose, buttonSave);

        // UISimpleList Test
        this.list = new UISimpleList(this, 125, SimpleScreen.getPaddedHeight(form), true);
        this.list.setElements(Lists.newArrayList());
        this.list.setPosition(4, 0);
        this.list.setElementSpacing(4);
        this.list.setUnselect(false);
        this.list.register(this);

        form.add(this.list);

        addToScreen(form);

        this.updateButtons();
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.details":
            case "button.format":
                this.showRaw = !this.showRaw;
                this.updateFormattingButton();

                final String currentContent = this.contentField.getText();
                if (showRaw) {
                    // Need to convert the content from sectional -> ampersand
                    this.contentField.setText(Page.asFriendlyText(currentContent));
                } else {
                    // Need to convert the content from ampersand -> sectional
                    this.contentField.setText(Page.asUglyText(currentContent));
                }
                break;
            case "button.add":
                new SimplePageCreate(this).display();
                break;
            case "button.save":
                if (manager.getPage() != null) {
                    final Page page = manager.getPage();
                    final String content = this.contentField.getText();
                    page.setContent(content);
                    manager.requestSavePage();
                }
                break;
            case "button.remove":
                if (manager.getPage() != null) {
                    manager.requestRemovePage(manager.getPage().getId());
                }
                break;
            case "button.close":
                close();
                break;
        }
    }

    @Subscribe
    public void onComboBoxSelect(UISelect.SelectEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "combobox.pages": {
                if (event.getNewValue() == null) {
                    this.contentField.setText("");
                } else {
                    final PageListEntry entry = (PageListEntry) event.getNewValue();
                    manager.requestPage(entry.getId());
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void refreshPageEntries() {
        final List<PageListElement> elementList = Lists.newArrayList();
        manager.getPageEntries()
                .forEach(entry -> elementList.add(new PageListElement(this, this.list, Text.of(entry.getName()), Text.EMPTY)));
        this.list.setElements(elementList);
//        pagesSelect.setOptions(manager.getPageEntries());
//
//        if (pagesSelect.getSelectedValue() != null) {
//            final Optional<PageListEntry> result = manager.getPageEntries().stream()
//                    .filter(entry -> entry.getId().equalsIgnoreCase(pagesSelect.getSelectedValue().getId()))
//                    .findFirst();
//
//            if (result.isPresent()) {
//                pagesSelect.setSelectedOption(result.get());
//            } else {
//                pagesSelect.selectFirst();
//            }
//        } else {
//            pagesSelect.selectFirst();
//        }
    }

    public void refreshPage() {
        if (manager.getPage() != null) {
            this.contentField.setText(manager.getPage().getContent());
        }
        this.updateButtons();
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

    private void updateButtons() {
        this.updateFormattingButton();
        this.buttonFormat.setEnabled((this.hasAnyPermission() && manager.getPage() != null));
        this.buttonRemove.setEnabled((this.hasRemovePermission() && manager.getPage() != null));
        this.buttonDetails.setEnabled((this.hasAnyPermission() && manager.getPage() != null));
    }

    private void updateFormattingButton() {
        this.buttonFormat.setText(this.showRaw ? "Raw" : TextFormatting.ITALIC + "Formatted");
        this.buttonFormat.setTooltip(this.showRaw ? "Showing raw text" : "Showing formatted text");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    protected static final class PageListElement extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();

        private final Text contentText;
        private final UILabel label;

        private PageListElement(MalisisGui gui, UISimpleList parent, Text text, Text contentText) {
            this(gui, parent, 32, 32, 2, 0, 4, text, contentText);
        }

        @SuppressWarnings("deprecation")
        private PageListElement(MalisisGui gui, UISimpleList parent, int imageWidth, int imageHeight, int imageX, int imageY, int
                padding, Text text, Text contentText) {
            super(gui);

            // Set parent
            this.parent = parent;

            // Create label
            this.label = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
            this.label.setPosition(padding, 2);

            // Set content text
            this.contentText = contentText;

            // Add image/label
//            this.add(this.image, this.label);
            this.add(this.label);

            // Set size
            this.setSize(((UIListContainer) this.getParent()).getContentWidth() - 3, 24);

            // Set padding
            this.setPadding(1, 1);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UISimpleList) {
                final UISimpleList parent = (UISimpleList) this.parent;

                final int width = parent.getContentWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 1 : 0);

                setSize(width, getHeight());

                if (this == parent.getSelected()) {
                    super.drawBackground(renderer, mouseX, mouseY, partialTick);
                }
            }
        }
    }
}
