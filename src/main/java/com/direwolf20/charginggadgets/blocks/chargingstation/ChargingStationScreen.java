package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.utils.MagicHelpers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationContainer> {
    private static final Identifier background = Identifier.fromNamespaceAndPath(ChargingGadgets.MOD_ID, "textures/gui/charging_station.png");

    private final ChargingStationContainer container;

    public ChargingStationScreen(ChargingStationContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (mouseX > (leftPos + 7) && mouseX < (leftPos + 7) + 18 && mouseY > (topPos + 7) && mouseY < (topPos + 7) + 73)
            guiGraphics.setTooltipForNextFrame(font, List.of(
                    Component.translatable("screen.charginggadgets.energy", MagicHelpers.withSuffix(this.container.getEnergy()), MagicHelpers.withSuffix(this.container.getMaxPower())),
                    this.container.getRemaining() <= 0 ?
                            Component.translatable("screen.charginggadgets.no_fuel") :
                            Component.translatable("screen.charginggadgets.burn_time", MagicHelpers.ticksInSeconds(this.container.getRemaining()))
            ), Optional.empty(), mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int maxHeight = 13;
        if (this.container.getMaxBurn() > 0) {
            int remaining = (this.container.getRemaining() * maxHeight) / this.container.getMaxBurn();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos + 66, topPos + 26 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1, 256, 256);
        }

        int maxEnergy = this.container.getMaxPower(), height = 70;
        if (maxEnergy > 0) {
            int remaining = (this.container.getEnergy() * height) / maxEnergy;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos + 8, topPos + 78 - remaining, 176, 84 - remaining, 16, remaining + 1, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(font, I18n.get("block.charginggadgets.charging_station"), 55, 8, ARGB.opaque(Color.DARK_GRAY.getRGB()), false);
    }
}
