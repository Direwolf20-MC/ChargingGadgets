package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneratorLoots extends LootTableProvider {
    public GeneratorLoots(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootParameterSets.BLOCK));
    }

    private static class Blocks extends BlockLootTables {
        @Override
        protected void addTables() {
            LootPool.Builder builder = LootPool.builder()
                    .name(ModBlocks.CHARGING_STATION.get().getRegistryName().toString())
                    .rolls(ConstantRange.of(1))
                    .acceptCondition(SurvivesExplosion.builder())
                    .addEntry(ItemLootEntry.builder(ModBlocks.CHARGING_STATION.get())
                            .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
// todo: figure out why these don't work
//                            .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
//                            .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
//                                    .addOperation("inv", "BlockEntityTag.inv", CopyNbt.Action.REPLACE)
//                                    .addOperation("energy", "BlockEntityTag.energy", CopyNbt.Action.REPLACE))
//                            .acceptFunction(SetContents.func_215920_b()
//                                    .func_216075_a(DynamicLootEntry.func_216162_a(new ResourceLocation("minecraft", "contents"))))
                    );

            this.registerLootTable(ModBlocks.CHARGING_STATION.get(), LootTable.builder().addLootPool(builder));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(ModBlocks.CHARGING_STATION.get());
        }
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((name, table) -> LootTableManager.func_227508_a_(validationtracker, name, table));
    }
}
