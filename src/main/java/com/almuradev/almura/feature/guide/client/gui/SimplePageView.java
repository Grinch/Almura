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
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageView extends SimpleScreen {

    private static final int INNER_PADDING = 2;

    @Inject
    private static ClientPageManager manager;

    private boolean showRaw = false;
    private UIButton buttonRemove, buttonAdd, buttonDetails, buttonFormat;
    private UISelect<String> pagesSelect;
    private UITextField contentField;

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
        contentField.setEditable(false);

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


        form.add(this.buttonRemove, this.pagesSelect, this.buttonDetails, this.buttonAdd, this.buttonFormat, contentField, buttonClose, buttonSave);

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
                    this.contentField.setText(TextSerializers.FORMATTING_CODE.serialize(TextSerializers.LEGACY_FORMATTING_CODE.deserialize
                            (currentContent)));
                } else {
                    // Need to convert the content from ampersand -> sectional
                    this.contentField.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(TextSerializers.FORMATTING_CODE.deserialize
                            (currentContent)));
                }
                break;
            case "button.add":
                new SimplePageCreate(this).display();
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
                manager.requestPage(event.getNewValue().toString());
            }
        }
    }

    public void refreshPages() {
        pagesSelect.setOptions(manager.getPageNames());
        // TODO Grinch, if you get a page refresh, might wanna check if their current page is still in the list and keep them selected on it
        pagesSelect.selectFirst();
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
}
