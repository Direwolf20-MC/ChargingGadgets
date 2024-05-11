package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ChargingGadgets.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var includeServer = event.includeServer();
        var includeClient = event.includeClient();
        var generator = event.getGenerator();
        var helper = event.getExistingFileHelper();
        var packOutput = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Client
        generator.addProvider(includeClient, new GeneratorLanguage(packOutput));
        generator.addProvider(includeClient, new GeneratorBlockStates(packOutput, event.getExistingFileHelper()));


        // Server
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(GeneratorLoots::new, LootContextParamSets.BLOCK)), event.getLookupProvider()));
        generator.addProvider(includeServer, new GeneratorRecipes(packOutput, lookupProvider));
        generator.addProvider(includeServer, new GeneratorBlockTags(packOutput, lookupProvider, event.getExistingFileHelper()));
        generator.addProvider(includeServer, new GeneratorItemModels(packOutput, event.getExistingFileHelper()));
    }

    static class GeneratorBlockStates extends BlockStateProvider {
        public GeneratorBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, ChargingGadgets.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            horizontalBlock(BlockRegistry.CHARGING_STATION.get(), models().orientableWithBottom(
                    BlockRegistry.CHARGING_STATION.getId().getPath(),
                    modLoc("block/charging_station_side"),
                    modLoc("block/charging_station_fronton"),
                    modLoc("block/charging_station_bottom"),
                    modLoc("block/charging_station_top")
            ));
        }
    }

    static class GeneratorItemModels extends ItemModelProvider {
        public GeneratorItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, ChargingGadgets.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            String path = BlockRegistry.CHARGING_STATION.getId().getPath();
            getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
        }

        @Override
        public String getName() {
            return "Item Models";
        }
    }

    static class GeneratorLanguage extends LanguageProvider {
        public GeneratorLanguage(PackOutput output) {
            super(output, ChargingGadgets.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            addBlock(BlockRegistry.CHARGING_STATION, "Charging Station");
            add("itemGroup.charginggadgets", "Charging Gadgets");
            add("screen.charginggadgets.energy", "Energy: %s/%s FE");
            add("screen.charginggadgets.no_fuel", "Fuel source empty");
            add("screen.charginggadgets.burn_time", "Burn time left: %ss");
        }
    }


    static class GeneratorLoots extends VanillaBlockLoot {
        @Override
        protected void generate() {
            dropSelf(BlockRegistry.CHARGING_STATION.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> knownBlocks = new ArrayList<>();
            knownBlocks.addAll(BlockRegistry.BLOCKS.getEntries().stream().map(DeferredHolder::get).toList());
            return knownBlocks;
        }
    }

    static class GeneratorRecipes extends RecipeProvider {
        public GeneratorRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }


        @Override
        protected void buildRecipes(RecipeOutput consumer) {
            Block block = BlockRegistry.CHARGING_STATION.get();
            ShapedRecipeBuilder
                    .shaped(RecipeCategory.REDSTONE, block)
                    .define('i', Tags.Items.INGOTS_IRON)
                    .define('r', Tags.Items.DUSTS_REDSTONE)
                    .define('l', Tags.Items.STORAGE_BLOCKS_COAL)
                    .define('d', Tags.Items.GEMS_LAPIS)
                    .pattern("iri")
                    .pattern("drd")
                    .pattern("ili")
                    .unlockedBy("has_diamonds", has(Tags.Items.GEMS_DIAMOND))
                    .save(consumer);
        }
    }

    static class GeneratorBlockTags extends BlockTagsProvider {
        public GeneratorBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, ChargingGadgets.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BlockRegistry.CHARGING_STATION.get());
        }
    }
}

