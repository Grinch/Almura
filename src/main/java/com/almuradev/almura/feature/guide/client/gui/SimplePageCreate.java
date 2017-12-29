/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageCreate extends SimpleScreen {

    private static final int PADDING = 4;

    @Inject
    private static ClientPageManager manager;

    private UITextField textFieldFileName, textFieldIndex, textFieldTitle;

    public SimplePageCreate(@Nullable GuiScreen parent) {
        super(parent);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        final UIForm form = new UIForm(this, 150, 135, I18n.format("almura.guide.create.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setClipContent(false);

        // File name
        final UILabel labelFileName = new UILabel(this, I18n.format("almura.guide.create.filename"));
        labelFileName.setAnchor(Anchor.TOP | Anchor.LEFT);

        this.textFieldFileName = new UITextField(this, "");
        this.textFieldFileName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldFileName.setPosition(0, SimpleScreen.getPaddedY(labelFileName, 1));
        this.textFieldFileName.setSize(UIComponent.INHERITED, 0);
        this.textFieldFileName.setFocused(true);

        // Index
        final UILabel labelIndex = new UILabel(this, I18n.format("almura.guide.create.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, this.textFieldFileName.isVisible() ? SimpleScreen.getPaddedY(textFieldFileName, PADDING) : PADDING);

        this.textFieldIndex = new UITextField(this, Integer.toString(0));
        this.textFieldIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldIndex.setPosition(0, SimpleScreen.getPaddedY(labelIndex, 1));
        this.textFieldIndex.setSize(UIComponent.INHERITED, 0);
        //this.textFieldIndex.setValidator(new Predicates.IntegerFilterPredicate());

        // Title
        final UILabel labelTitle = new UILabel(this, I18n.format("almura.guide.create.title"));
        labelTitle.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelTitle.setPosition(0, this.textFieldIndex.isVisible() ? SimpleScreen.getPaddedY(textFieldIndex, PADDING) : PADDING);

        this.textFieldTitle = new UITextField(this, "");
        this.textFieldTitle.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldTitle.setPosition(0, SimpleScreen.getPaddedY(labelTitle, 1));
        this.textFieldTitle.setSize(UIComponent.INHERITED, 0);
        //this.textFieldTitle.setValidator(new Predicates.StringLengthPredicate(1, 100));

        // Save/Cancel
        final UIButton buttonSave = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .size(40, 20)
                .listener(this)
                .build("guide.create.save");

        final UIButton buttonCancel = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(SimpleScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
                .size(40, 20)
                .listener(this)
                .build("guide.create.cancel");

        form.add(labelFileName, this.textFieldFileName,
                labelIndex, this.textFieldIndex,
                labelTitle, this.textFieldTitle,
                buttonCancel, buttonSave);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "guide.create.cancel":
                close();
                break;
            case "guide.create.save":
                textFieldFileName.setText(textFieldFileName.getText().trim());
                // TODO Grinch, Index can be empty, just assume 0
                // TODO Grinch, Title being empty should just use the filename
                if (textFieldFileName.getText().isEmpty() || textFieldIndex.getText().isEmpty() || textFieldTitle.getText().isEmpty()) {
                    break;
                }

                // TODO Grinch, page ids must be lowercase
                final String id = this.textFieldFileName.getText();
                // TODO Grinch, validators above need to be restored
                final int index = Integer.parseInt(this.textFieldIndex.getText());
                // TODO Grinch, where is Name?
                final String name = this.textFieldTitle.getText();
                final String title = this.textFieldTitle.getText();

                manager.requestNewPage(id, index, name, title);

                close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
