package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public final class GeneratorRecipes extends RecipeProvider {
    public GeneratorRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        Block block = ModBlocks.CHARGING_STATION.get();
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
