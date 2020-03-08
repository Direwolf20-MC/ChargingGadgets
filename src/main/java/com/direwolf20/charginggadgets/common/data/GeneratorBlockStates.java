package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ChargingStationBlock;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelBuilder;

final class GeneratorBlockStates extends BlockStateProvider {
    public GeneratorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ChargingGadgets.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        assert ModBlocks.CHARGING_STATION.get().getRegistryName() != null;

        ResourceLocation side = modLoc("blocks/charging_station_side");
        ModelBuilder on = models().cube(
                ModBlocks.CHARGING_STATION.get().getRegistryName().getPath() + "_on",
                modLoc("blocks/charging_station_bottom"),
                modLoc("blocks/charging_station_top"),
                modLoc("blocks/charging_station_fronton"),
                side, side, side
        ).texture("particle", side);

        ModelBuilder off = models().cube(
                ModBlocks.CHARGING_STATION.get().getRegistryName().getPath(),
                modLoc("blocks/charging_station_bottom"),
                modLoc("blocks/charging_station_top"),
                modLoc("blocks/charging_station_frontoff"),
                side, side, side
        ).texture("particle", side);

        getVariantBuilder(ModBlocks.CHARGING_STATION.get())
                .forAllStates(state -> {
                    Direction dir = state.get(ChargingStationBlock.FACING);
                    return ConfiguredModel.builder().modelFile(state.get(ChargingStationBlock.LIT) ? on : off).rotationY(
                            dir.getAxis().isVertical() ? 0 : (((int) dir.getHorizontalAngle()) + 180) % 360
                    ).build();
                });
    }
}
