package com.direwolf20.charginggadgets.client.screens;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChargingStationScreen  extends ContainerScreen<ChargingStationContainer> {

    private static final ResourceLocation background = new ResourceLocation(ChargingGadgets.MOD_ID, "textures/gui/charging_station.png");

    private ChargingStationContainer container;
    private List<String> toolTip = new ArrayList<>();

    public ChargingStationScreen(ChargingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        this.renderHoveredToolTip(mouseX, mouseY);

        if (mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73)
            this.renderTooltip(Arrays.asList(
                    String.format("Energy: %s FE", withSuffix(this.container.getEnergy())),
                    this.container.getTile().getRemainingBurn() <= 0 ?
                            "No fuel" :
                            String.format("Burn time left: %ss", ticksInSeconds(this.container.getTile().getRemainingBurn()))
            ), mouseX, mouseY);

        toolTip.clear();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(background);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        int maxHeight = 13;
        if (this.container.getTile().getMaxBurn() > 0) {
            int remaining = (this.container.getTile().getRemainingBurn() * maxHeight) / this.container.getTile().getMaxBurn();
            this.blit(guiLeft + 66, guiTop + 26 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1);
        }

        this.container.getTile().getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            int height = 68;
            if (energy.getMaxEnergyStored() > 0) {
                int remaining = (energy.getEnergyStored() * height) / energy.getMaxEnergyStored();
                this.blit(guiLeft + 8, guiTop + 76 - remaining, 176, 83 - remaining, 16, remaining + 1);
            }
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(
                I18n.format("block.buildinggadgets.charging_station"),
                55,
                8,
                Color.DARK_GRAY.getRGB()
        );
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }

    private static final BigDecimal TWENTY = new BigDecimal(20);
    public static String ticksInSeconds(int ticks) {
        BigDecimal value = new BigDecimal(ticks);
        value = value.divide(TWENTY, 1, RoundingMode.HALF_UP);
        return value.toString();
    }
}

