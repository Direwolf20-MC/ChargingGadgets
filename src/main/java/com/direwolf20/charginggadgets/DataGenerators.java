package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ChargingGadgets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var includeServer = event.includeServer();
        var includeClient = event.includeClient();
        var generator = event.getGenerator();
        var helper = event.getExistingFileHelper();
        var packOutput = event.getGenerator().getPackOutput();

        // Client
        generator.addProvider(includeClient, new GeneratorLanguage(packOutput));
        generator.addProvider(includeClient, new GeneratorBlockStates(packOutput, event.getExistingFileHelper()));
        generator.addProvider(includeClient, new GeneratorLoots(packOutput));

        // Server
        generator.addProvider(includeServer, new GeneratorRecipes(packOutput));
        generator.addProvider(includeServer, new GeneratorBlockTags(packOutput, event.getLookupProvider(), event.getExistingFileHelper()));
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


    static class GeneratorLoots extends LootTableProvider {
        public GeneratorLoots(PackOutput output) {
            super(output, Set.of(), ImmutableList.of(
                    new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
            ));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
            map.forEach((name, table) -> LootTables.validate(validationContext, name, table));
        }

        private static class Blocks extends BlockLootSubProvider {
            protected Blocks() {
                super(Set.of(), FeatureFlags.REGISTRY.allFlags());
            }

            @Override
            protected void generate() {
                LootPool.Builder builder = LootPool.lootPool()
                        .name(BlockRegistry.CHARGING_STATION.getId().toString())
                        .setRolls(ConstantValue.exactly(1))
                        .when(ExplosionCondition.survivesExplosion())
                        .add(LootItem.lootTableItem(BlockRegistry.CHARGING_STATION.get())
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)));

                this.add(BlockRegistry.CHARGING_STATION.get(), LootTable.lootTable().withPool(builder));
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return Collections.singletonList(BlockRegistry.CHARGING_STATION.get());
            }
        }
    }

    static class GeneratorRecipes extends RecipeProvider {
        public GeneratorRecipes(PackOutput output) {
            super(output);
        }


        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
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

