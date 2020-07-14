package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.common.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ChargingStationBlock;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;

import java.util.Objects;

final class GeneratorBlockStates extends BlockStateProvider {
    public GeneratorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ChargingGadgets.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        models().orientableWithBottom(
                Objects.requireNonNull(ModBlocks.CHARGING_STATION.get().getRegistryName()).getPath(),
                modLoc("blocks/charging_station_side"),
                modLoc("blocks/charging_station_fronton"),
                modLoc("blocks/charging_station_bottom"),
                modLoc("blocks/charging_station_top")
        );
    }
}
