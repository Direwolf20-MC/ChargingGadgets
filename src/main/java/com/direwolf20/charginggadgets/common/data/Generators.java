package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ChargingStationBlock;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

/**
 * @implNote I've chosen to keep the small limited generators to this class but to
 *           expand the more complex or larger generators to a different file.
 */
@Mod.EventBusSubscriber(modid = ChargingGadgets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Generators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if( event.includeServer() ) {
            generator.addProvider(new GeneratorRecipes(generator));
        }

        if( event.includeClient() ) {
            generator.addProvider(new GeneratorLanguage(generator));
            generator.addProvider(new GeneratorBlockStates(generator, event.getExistingFileHelper()));
            generator.addProvider(new GeneratorItemModels(generator, event.getExistingFileHelper()));
        }
    }

    private static final class GeneratorItemModels extends ItemModelProvider {
        public GeneratorItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, ChargingGadgets.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            String path = ModBlocks.CHARGING_STATION.get().getRegistryName().getPath();
            getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
        }

        @Override
        public String getName() {
            return "Item Models";
        }
    }

    private static final class GeneratorBlockStates extends BlockStateProvider {
        public GeneratorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
            super(gen, ChargingGadgets.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            ResourceLocation side = modLoc("blocks/charging_station_side");
            // Sorry for the formatting on this one, it's because we have to define all the sides :(
            getVariantBuilder(ModBlocks.CHARGING_STATION.get()).partialState().with(ChargingStationBlock.LIT, false).addModels(
                    ConfiguredModel.builder().modelFile(cube(
                            ModBlocks.CHARGING_STATION.get().getRegistryName().getPath(),
                            modLoc("blocks/charging_station_bottom"),
                            modLoc("blocks/charging_station_top"),
                            modLoc("blocks/charging_station_frontoff"),
                            side, side, side
                    ).texture("particle", side)).build()
            ).with(ChargingStationBlock.LIT, true).addModels(
                    ConfiguredModel.builder().modelFile(cube(
                            ModBlocks.CHARGING_STATION.get().getRegistryName().getPath(),
                            modLoc("blocks/charging_station_bottom"),
                            modLoc("blocks/charging_station_top"),
                            modLoc("blocks/charging_station_fronton"),
                            side, side, side
                    ).texture("particle", side)).build()
            );
        }
    }
}

