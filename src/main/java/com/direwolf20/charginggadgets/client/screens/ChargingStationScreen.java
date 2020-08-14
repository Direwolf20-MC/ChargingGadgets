package com.direwolf20.charginggadgets.client.screens;

import com.direwolf20.charginggadgets.common.ChargingGadgets;
import com.direwolf20.charginggadgets.common.Config;
import com.direwolf20.charginggadgets.common.capabilities.ChargerEnergyStorage;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import com.direwolf20.charginggadgets.common.utils.MagicHelpers;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.FurnaceScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChargingStationScreen  extends ContainerScreen<ChargingStationContainer> {
    private static final ResourceLocation background = new ResourceLocation(ChargingGadgets.MOD_ID, "textures/gui/charging_station.png");

    private final ChargingStationContainer container;
    public ChargingStationScreen(ChargingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        this.func_230459_a_(stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
        if (mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73)
            this.renderTooltip(stack, LanguageMap.getInstance().func_244260_a(Arrays.asList(
                    new TranslationTextComponent("screen.charginggadgets.energy", MagicHelpers.withSuffix(this.container.getEnergy()), MagicHelpers.withSuffix(this.container.getMaxPower())),
                    this.container.getRemaining() <= 0 ?
                            new TranslationTextComponent("screen.charginggadgets.no_fuel") :
                            new TranslationTextComponent("screen.charginggadgets.burn_time", MagicHelpers.ticksInSeconds(this.container.getRemaining()))
            )), mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(background);
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);

        int maxHeight = 13;
        if (this.container.getMaxBurn() > 0) {
            int remaining = (this.container.getRemaining() * maxHeight) / this.container.getMaxBurn();
            this.blit(stack, guiLeft + 66, guiTop + 26 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1);
        }

        int maxEnergy = this.container.getMaxPower(), height = 70;
        if (maxEnergy > 0) {
            int remaining = (this.container.getEnergy() * height) / maxEnergy;
            this.blit(stack, guiLeft + 8, guiTop + 78 - remaining, 176, 84 - remaining, 16, remaining + 1);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.charginggadgets.charging_station"), 55, 8, Color.DARK_GRAY.getRGB());
    }
}

