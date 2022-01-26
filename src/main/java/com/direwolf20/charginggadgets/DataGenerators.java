package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ChargingGadgets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            generator.addProvider(new GeneratorRecipes(generator));
            generator.addProvider(new GeneratorLoots(generator));
        }

        if (event.includeClient()) {
            generator.addProvider(new GeneratorBlockTags(generator, event.getExistingFileHelper()));
            generator.addProvider(new GeneratorLanguage(generator));
            generator.addProvider(new GeneratorBlockStates(generator, event.getExistingFileHelper()));
            generator.addProvider(new GeneratorItemModels(generator, event.getExistingFileHelper()));
        }
    }

    static class GeneratorBlockStates extends BlockStateProvider {
        public GeneratorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
            super(gen, ChargingGadgets.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            horizontalBlock(BlockRegistry.CHARGING_STATION.get(), models().orientableWithBottom(
                    BlockRegistry.CHARGING_STATION.get().getRegistryName().getPath(),
                    modLoc("blocks/charging_station_side"),
                    modLoc("blocks/charging_station_fronton"),
                    modLoc("blocks/charging_station_bottom"),
                    modLoc("blocks/charging_station_top")
            ));
        }
    }

    static class GeneratorItemModels extends ItemModelProvider {
        public GeneratorItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, ChargingGadgets.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            String path = BlockRegistry.CHARGING_STATION.get().getRegistryName().getPath();
            getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
        }

        @Override
        public String getName() {
            return "Item Models";
        }
    }

    static class GeneratorLanguage extends LanguageProvider {
        public GeneratorLanguage(DataGenerator gen) {
            super(gen, ChargingGadgets.MOD_ID, "en_us");
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
        public GeneratorLoots(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
        }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
            return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK));
        }

        private static class Blocks extends BlockLoot {
            @Override
            protected void addTables() {
                LootPool.Builder builder = LootPool.lootPool()
                        .name(BlockRegistry.CHARGING_STATION.get().getRegistryName().toString())
                        .setRolls(ConstantValue.exactly(1))
                        .when(ExplosionCondition.survivesExplosion())
                        .add(LootItem.lootTableItem(BlockRegistry.CHARGING_STATION.get())
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)));

                this.add(BlockRegistry.CHARGING_STATION.get(), LootTable.lootTable().withPool(builder));
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return ImmutableList.of(BlockRegistry.CHARGING_STATION.get());
            }
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
            map.forEach((name, table) -> LootTables.validate(validationtracker, name, table));
        }
    }

    static class GeneratorRecipes extends RecipeProvider {
        public GeneratorRecipes(DataGenerator generator) {
            super(generator);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
            Block block = BlockRegistry.CHARGING_STATION.get();
            ShapedRecipeBuilder
                    .shaped(block)
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
        public GeneratorBlockTags(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
            super(generator, ChargingGadgets.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            tag(BlockTags.createOptional(BlockRegistry.CHARGING_STATION.get().getRegistryName()))
                    .addTags(BlockTags.MINEABLE_WITH_PICKAXE);
        }
    }
}

