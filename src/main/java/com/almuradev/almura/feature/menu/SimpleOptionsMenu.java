/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.client.config.ClientCategory;
import com.almuradev.almura.feature.hud.HUDType;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.slider.UISliderBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.base.Converter;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class SimpleOptionsMenu extends SimpleScreen {

    private static final OptionsConverter OPTIONS_CONVERTER = new OptionsConverter();
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_PADDING = 5;

    private UIButton buttonHudType;
    private UICheckBox checkboxWorldCompassWidget, checkboxLocationWidget;
    private UISlider<Integer> sliderOriginHudOpacity;

    public SimpleOptionsMenu(GuiOptions parentOptions) {
        super(parentOptions);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        final ClientCategory config = StaticAccess.config.get().client;
        this.saveAndLoad();

        final UILabel titleLabel = new UILabel(this, I18n.format("almura.menu_button.options"));
        titleLabel.setFontOptions(FontColors.WHITE_FO);
        titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        this.addToScreen(titleLabel);

        this.buttonHudType = new UIButtonBuilder(this)
                .text("HUD: " + config.hud.substring(0, 1).toUpperCase() + config.hud.substring(1))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(-(CONTROL_WIDTH / 2 + CONTROL_PADDING), getPaddedY(titleLabel, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.hudType");

        final boolean isOrigin = config.hud.equalsIgnoreCase(HUDType.ORIGIN);
        this.sliderOriginHudOpacity = new UISliderBuilder(this, Converter.<Float, Integer>from(f -> (int) (f * 255), i -> (float) i / 255))
                .text("HUD Opacity: %d")
                .value(config.originHudOpacity)
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(-(CONTROL_WIDTH / 2 + CONTROL_PADDING),  getPaddedY(this.buttonHudType, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .enabled(isOrigin)
                .build("slider.originHudOpacity");
        this.sliderOriginHudOpacity.setAlpha(isOrigin ? 255 : 128);

        final UISlider<OptionsConverter.Options> sliderChestDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Chest Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.chestRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING,  getPaddedY(titleLabel, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.chestRenderDistance");

        final UISlider<OptionsConverter.Options> sliderSignTextDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Sign Text Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.signTextRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING, getPaddedY(sliderChestDistance, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.signTextRenderDistance");

        final UISlider<OptionsConverter.Options> sliderItemFrameDistance = new UISliderBuilder(this, OPTIONS_CONVERTER)
                .text("Item Frame Distance: %s")
                .value(Arrays.stream(OptionsConverter.Options.values())
                        .filter(o -> o.value == config.itemFrameRenderDistance)
                        .findFirst()
                        .orElse(OptionsConverter.Options.DEFAULT))
                .size(CONTROL_WIDTH, CONTROL_HEIGHT)
                .position(CONTROL_WIDTH / 2 + CONTROL_PADDING, getPaddedY(sliderSignTextDistance, CONTROL_PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("slider.itemFrameRenderDistance");

        final UIButton buttonDone = new UIButtonBuilder(this)
                .text(I18n.format("gui.done"))
                .size(200, CONTROL_HEIGHT)
                .position(0, -12)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .listener(this)
                .build("button.done");
        /**
         * TODO:
         * These need a builder
         */

        final boolean displayWorldCompassWidget = config.displayWorldCompassWidget;
        final UICheckBox checkboxWorldCompassWidget = new UICheckBox(this);
        checkboxWorldCompassWidget.setText(TextFormatting.WHITE + "Display World Compass Widget");
        checkboxWorldCompassWidget.setAnchor(Anchor.TOP | Anchor.CENTER);
        checkboxWorldCompassWidget.setPosition(this.buttonHudType.getX(),  getPaddedY(sliderItemFrameDistance, 10));
        checkboxWorldCompassWidget.setChecked(displayWorldCompassWidget);
        checkboxWorldCompassWidget.setName("checkbox.world_compass_widget");
        checkboxWorldCompassWidget.register(this);

        final boolean displayLocationWidget = config.displayLocationWidget;
        final UICheckBox checkboxLocationWidget = new UICheckBox(this);
        checkboxLocationWidget.setText(TextFormatting.WHITE + "Display Location Widget");
        checkboxLocationWidget.setAnchor(Anchor.TOP | Anchor.CENTER);
        checkboxLocationWidget.setPosition(this.buttonHudType.getX(),  getPaddedY(checkboxWorldCompassWidget, CONTROL_PADDING));
        checkboxLocationWidget.setChecked(displayLocationWidget);
        checkboxLocationWidget.setName("checkbox.location_widget");
        checkboxLocationWidget.register(this);

        final boolean displayNumericHUDValues = config.displayNumericHUDValues;
        final UICheckBox checkboxNumericHUDValues = new UICheckBox(this);
        checkboxNumericHUDValues.setText(TextFormatting.WHITE + "Display Numeric HUD Values");
        checkboxNumericHUDValues.setAnchor(Anchor.TOP | Anchor.CENTER);
        checkboxNumericHUDValues.setPosition(this.buttonHudType.getX(),  getPaddedY(checkboxLocationWidget, CONTROL_PADDING));
        checkboxNumericHUDValues.setChecked(displayNumericHUDValues);
        checkboxNumericHUDValues.setName("checkbox.numeric_hud_values");
        checkboxNumericHUDValues.register(this);

        addToScreen(this.buttonHudType, this.sliderOriginHudOpacity, checkboxWorldCompassWidget, checkboxLocationWidget, checkboxNumericHUDValues, sliderChestDistance, sliderSignTextDistance, sliderItemFrameDistance, buttonDone);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private void saveAndLoad() {
        StaticAccess.config.save();
        StaticAccess.config.load();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName()) {
            case "button.hudType":
                // Check if the current HUD is the Origin HUD
                boolean isOrigin = StaticAccess.config.get().client.hud.equalsIgnoreCase(HUDType.ORIGIN);
                StaticAccess.config.get().client.hud = isOrigin ? HUDType.VANILLA : HUDType.ORIGIN;
                // Flip the boolean since we're now on the vanilla HUD
                isOrigin = !isOrigin;

                this.buttonHudType.setText("HUD: " + (isOrigin ? "Origin" : "Vanilla"));
                this.sliderOriginHudOpacity.setAlpha(isOrigin ? 255 : 128);
                this.sliderOriginHudOpacity.setEnabled(isOrigin);

                if (isOrigin) {
                    this.sliderOriginHudOpacity.setTooltip((UITooltip) null);
                } else {
                    this.sliderOriginHudOpacity.setTooltip("Only available when Origin HUD is in use.");
                }

                break;
            case "button.done":
                this.close();
                break;
        }

        saveAndLoad();
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "slider.originHudOpacity":
                StaticAccess.config.get().client.originHudOpacity = (int) event.getNewValue();
                break;
            case "slider.chestRenderDistance" :
                StaticAccess.config.get().client.chestRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.signTextRenderDistance" :
                StaticAccess.config.get().client.signTextRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "slider.itemFrameRenderDistance" :
                StaticAccess.config.get().client.itemFrameRenderDistance = ((OptionsConverter.Options) event.getNewValue()).value;
                break;
            case "checkbox.world_compass_widget" :
                StaticAccess.config.get().client.displayWorldCompassWidget = (boolean) event.getNewValue();
                break;
            case "checkbox.location_widget" :
                StaticAccess.config.get().client.displayLocationWidget = (boolean) event.getNewValue();
                break;
            case "checkbox.numeric_hud_values" :
                StaticAccess.config.get().client.displayNumericHUDValues = (boolean) event.getNewValue();
                break;
        }

        saveAndLoad();
    }


    private static class OptionsConverter extends Converter<Float, OptionsConverter.Options> {
        @Override
        protected Options doForward(Float val) {
            final int i = Math.round(val * (Options.values().length - 1));
            return Arrays.stream(Options.values())
                    .filter(o -> o.index == i)
                    .findFirst()
                    .orElse(Options.DEFAULT);
        }

        @Override
        protected Float doBackward(Options option) {
            if (Options.values().length == 0){
                return 0f;
            } else {
                return (float) option.index / (Options.values().length - 1);
            }
        }

        protected enum Options {
            DEFAULT("Default", 0, 0),
            V16("16", 1, 16),
            V32("32", 2, 32),
            V64("64", 3, 64),
            V128("128", 4, 128);

            public final String name;
            public final int index;
            public final int value;

            Options(String name, int index, int value) {
                this.name = name;
                this.index = index;
                this.value = value;
            }

            @Override
            public String toString() {
                return this.name;
            }
        }
    }
}
