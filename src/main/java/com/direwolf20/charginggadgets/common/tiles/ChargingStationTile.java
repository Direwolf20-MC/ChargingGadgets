package com.direwolf20.charginggadgets.common.tiles;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.tileentity.TileEntity;

public class ChargingStationTile extends TileEntity {
    @SuppressWarnings("ConstantConditions")
    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());
    }
}
