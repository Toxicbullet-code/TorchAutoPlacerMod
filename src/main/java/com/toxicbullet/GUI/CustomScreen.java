package com.toxicbullet.GUI;

import com.toxicbullet.AutoPlace.AutoPlace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CustomScreen extends Screen {

    private EditBox intervalBox;

    public CustomScreen() {
        super(Component.literal("Auto Place Settings"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 1. Enable / Disable Button
        this.addRenderableWidget(Button.builder(Component.literal(AutoPlace.isEnabled ? "Enabled" : "Disabled"), button -> {
            AutoPlace.isEnabled = !AutoPlace.isEnabled;
            button.setMessage(Component.literal(AutoPlace.isEnabled ? "Enabled" : "Disabled"));
        }).bounds(centerX - 100, centerY - 80, 200, 20).build());

        // Get the display name of the currently saved item, defaulting to Torch if null
        String itemName = AutoPlace.selectedItem != null ? new ItemStack(AutoPlace.selectedItem).getHoverName().getString() : "Torch";

        // 2. Set Item From Hand Button
        this.addRenderableWidget(Button.builder(Component.literal("Item: " + itemName), button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ItemStack held = mc.player.getMainHandItem();
                if (!held.isEmpty()) {
                    AutoPlace.selectedItem = held.getItem();
                    button.setMessage(Component.literal("Item: " + held.getHoverName().getString()));
                } else {
                    button.setMessage(Component.literal("Hand is empty!"));
                }
            }
        }).bounds(centerX - 100, centerY - 55, 200, 20).build());

        // 3. Interval Input Box (EditBox)
        intervalBox = new EditBox(this.font, centerX - 100, centerY - 15, 200, 20, Component.literal("Interval"));
        intervalBox.setMaxLength(4);
        intervalBox.setValue(String.valueOf(AutoPlace.blockInterval));
        intervalBox.setResponder(value -> {
            try {
                if (!value.isEmpty()) {
                    int parsed = Integer.parseInt(value);
                    if (parsed > 0) {
                        AutoPlace.blockInterval = parsed;
                    }
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(intervalBox);

        // 4. Light-Level Mode Toggle Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Light Check: " + (AutoPlace.lightModeEnabled ? "ON" : "OFF")),
                        button -> {
                            AutoPlace.lightModeEnabled = !AutoPlace.lightModeEnabled;
                            button.setMessage(Component.literal("Light Check: " + (AutoPlace.lightModeEnabled ? "ON" : "OFF")));
                        })
                .bounds(centerX - 100, centerY + 15, 200, 20)
                .build());

        // 5. Close Button
        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(centerX - 100, centerY + 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Label above the interval text box for clarity
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        guiGraphics.drawString(this.font, "Distance Interval (Blocks):", centerX - 100, centerY - 28, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (intervalBox != null && intervalBox.isFocused() && keyCode == 256) {
            intervalBox.setFocused(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}