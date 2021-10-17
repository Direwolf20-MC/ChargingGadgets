package com.direwolf20.charginggadgets.client.screens;

import com.direwolf20.charginggadgets.common.ChargingGadgets;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import com.direwolf20.charginggadgets.common.utils.MagicHelpers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.TranslatableComponent;

import java.awt.*;
import java.util.Arrays;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationContainer> {
    private static final ResourceLocation background = new ResourceLocation(ChargingGadgets.MOD_ID, "textures/gui/charging_station.png");

    private final ChargingStationContainer container;

    public ChargingStationScreen(ChargingStationContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        this.renderTooltip(stack, mouseX, mouseY); // @mcp: renderTooltip = renderHoveredToolTip
        if (mouseX > (leftPos + 7) && mouseX < (leftPos + 7) + 18 && mouseY > (topPos + 7) && mouseY < (topPos + 7) + 73)
            this.renderTooltip(stack, Language.getInstance().getVisualOrder(Arrays.asList(
                    new TranslatableComponent("screen.charginggadgets.energy", MagicHelpers.withSuffix(this.container.getEnergy()), MagicHelpers.withSuffix(this.container.getMaxPower())),
                    this.container.getRemaining() <= 0 ?
                            new TranslatableComponent("screen.charginggadgets.no_fuel") :
                            new TranslatableComponent("screen.charginggadgets.burn_time", MagicHelpers.ticksInSeconds(this.container.getRemaining()))
            )), mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, background);
        this.blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int maxHeight = 13;
        if (this.container.getMaxBurn() > 0) {
            int remaining = (this.container.getRemaining() * maxHeight) / this.container.getMaxBurn();
            this.blit(stack, leftPos + 66, topPos + 26 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1);
        }

        int maxEnergy = this.container.getMaxPower(), height = 70;
        if (maxEnergy > 0) {
            int remaining = (this.container.getEnergy() * height) / maxEnergy;
            this.blit(stack, leftPos + 8, topPos + 78 - remaining, 176, 84 - remaining, 16, remaining + 1);
        }
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().font.draw(stack, I18n.get("block.charginggadgets.charging_station"), 55, 8, Color.DARK_GRAY.getRGB());
    }
}

