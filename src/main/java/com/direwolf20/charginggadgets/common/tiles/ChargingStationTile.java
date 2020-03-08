package com.direwolf20.charginggadgets.common.tiles;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class ChargingStationTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());
    }

    public int getRemainingBurn() {
        return 0;
    }

    public int getMaxBurn() {
        return 0;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Charging Station Tile");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ChargingStationContainer(i, playerEntity.world, this.pos, playerInventory, playerEntity);
    }

    @Override
    public void tick() {

    }
}
