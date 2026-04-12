package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ChargingGadgets.MOD_ID)
public final class DataGenerators {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();

        generator.addProvider(true, new GeneratorLanguage(packOutput));
        generator.addProvider(true, new GeneratorModels(packOutput));
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(GeneratorLoots::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(true, new GeneratorRecipes.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new GeneratorBlockTags(packOutput, lookupProvider));
    }

    static class GeneratorModels extends ModelProvider {
        public GeneratorModels(PackOutput output) {
            super(output, ChargingGadgets.MOD_ID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            // The charging station is a horizontally-rotated block with different faces
            // Custom texture mapping because textures use "fronton" not "front"
            Block block = BlockRegistry.CHARGING_STATION.get();
            Identifier modId = Identifier.fromNamespaceAndPath(ChargingGadgets.MOD_ID, "block/charging_station");
            TextureMapping mapping = new TextureMapping()
                    .put(TextureSlot.SIDE, new Material(modId.withSuffix("_side")))
                    .put(TextureSlot.FRONT, new Material(modId.withSuffix("_fronton")))
                    .put(TextureSlot.TOP, new Material(modId.withSuffix("_top")))
                    .put(TextureSlot.BOTTOM, new Material(modId.withSuffix("_bottom")));

            Identifier modelId = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(
                    block,
                    mapping,
                    blockModels.modelOutput
            );
            blockModels.blockStateOutput.accept(
                    BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelId))
                            .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
            );
            // BlockItem model is auto-resolved from the block model by ModelProvider
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
        public GeneratorLoots(HolderLookup.Provider p_344962_) {
            super(p_344962_);
        }

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
        public GeneratorRecipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            Block block = BlockRegistry.CHARGING_STATION.get();
            shaped(RecipeCategory.REDSTONE, block)
                    .define('i', Tags.Items.INGOTS_IRON)
                    .define('r', Tags.Items.DUSTS_REDSTONE)
                    .define('l', Tags.Items.STORAGE_BLOCKS_COAL)
                    .define('d', Tags.Items.GEMS_LAPIS)
                    .pattern("iri")
                    .pattern("drd")
                    .pattern("ili")
                    .unlockedBy("has_diamonds", has(Tags.Items.GEMS_DIAMOND))
                    .save(output);
        }

        public static class Runner extends RecipeProvider.Runner {
            public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
                super(output, lookupProvider);
            }

            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new GeneratorRecipes(registries, output);
            }

            @Override
            public String getName() {
                return "Charging Gadgets Recipes";
            }
        }
    }

    static class GeneratorBlockTags extends BlockTagsProvider {
        public GeneratorBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, ChargingGadgets.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BlockRegistry.CHARGING_STATION.get());
        }
    }
}
