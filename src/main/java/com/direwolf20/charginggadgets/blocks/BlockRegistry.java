package com.direwolf20.charginggadgets.blocks;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationBlock;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationContainer;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationItem;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ChargingGadgets.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChargingGadgets.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, ChargingGadgets.MOD_ID);

    public static final DeferredHolder<Block, ChargingStationBlock> CHARGING_STATION = BLOCKS.register("charging_station", ChargingStationBlock::new);

    /**
     * Tile Entities
     */
    public static final Supplier<BlockEntityType<ChargingStationTile>> CHARGING_STATION_TILE =
            TILES_ENTITIES.register("charging_station_tile", () -> BlockEntityType.Builder.of(ChargingStationTile::new, CHARGING_STATION.get()).build(null));

    /**
     * Containers?
     */
    public static final Supplier<MenuType<ChargingStationContainer>> CHARGING_STATION_CONTAINER = CONTAINERS.register("charging_station_container", () -> IMenuTypeExtension.create(ChargingStationContainer::new));

    /**
     * For now I'm adding items into here, it doesn't make much sense but nor does an items package for a mod with no
     * items... so... when we add items. Move this!
     */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ChargingGadgets.MOD_ID);
    public static final Supplier<Item> CHARGING_STATION_BI = ITEMS.register("charging_station", () -> new ChargingStationItem(CHARGING_STATION.get(), new Item.Properties()));
}
