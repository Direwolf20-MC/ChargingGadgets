package com.direwolf20.charginggadgets.common.container;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class ChargingStationContainer extends Container {
    private ChargingStationTile tile;

    public ChargingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);
        BlockPos pos = extraData.readBlockPos();
        this.tile = (ChargingStationTile) playerInventory.player.world.getTileEntity(pos);
    }

    public ChargingStationContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);
        this.tile = (ChargingStationTile) world.getTileEntity(pos);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    public ChargingStationTile getTile() {
        return tile;
    }

    public int getEnergy() {
        return 0;
    }
}
