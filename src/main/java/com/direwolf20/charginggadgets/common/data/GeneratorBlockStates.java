package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ChargingStationBlock;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.client.renderer.model.BlockPartRotation;
import net.minecraft.data.DataGenerator;
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
        ResourceLocation side = modLoc("blocks/charging_station_side");
        ModelBuilder on = cube(
                ModBlocks.CHARGING_STATION.get().getRegistryName().getPath() + "_on",
                modLoc("blocks/charging_station_bottom"),
                modLoc("blocks/charging_station_top"),
                modLoc("blocks/charging_station_fronton"),
                side, side, side
        ).texture("particle", side);

        ModelBuilder off = cube(
                ModBlocks.CHARGING_STATION.get().getRegistryName().getPath() + "_on",
                modLoc("blocks/charging_station_bottom"),
                modLoc("blocks/charging_station_top"),
                modLoc("blocks/charging_station_fronton"),
                side, side, side
        ).texture("particle", side);

        // Sorry for the formatting on this one, it's because we have to define all the sides :(
        getVariantBuilder(ModBlocks.CHARGING_STATION.get())
                .partialState().with(ChargingStationBlock.FACING, Direction.NORTH)  .with(ChargingStationBlock.LIT, true)   .setModels(ConfiguredModel.builder().modelFile(on).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.SOUTH)  .with(ChargingStationBlock.LIT, true)   .setModels(ConfiguredModel.builder().modelFile(on).rotationY(180).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.WEST)   .with(ChargingStationBlock.LIT, true)   .setModels(ConfiguredModel.builder().modelFile(on).rotationY(270).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.EAST)   .with(ChargingStationBlock.LIT, true)   .setModels(ConfiguredModel.builder().modelFile(on).rotationY(90).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.NORTH)  .with(ChargingStationBlock.LIT, false)  .setModels(ConfiguredModel.builder().modelFile(off).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.SOUTH)  .with(ChargingStationBlock.LIT, false)  .setModels(ConfiguredModel.builder().modelFile(off).rotationY(180).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.WEST)   .with(ChargingStationBlock.LIT, false)  .setModels(ConfiguredModel.builder().modelFile(off).rotationY(270).build())
                .partialState().with(ChargingStationBlock.FACING, Direction.EAST)   .with(ChargingStationBlock.LIT, false)  .setModels(ConfiguredModel.builder().modelFile(off).rotationY(90).build());
    }
}
