package com.direwolf20.charginggadgets.common.blocks;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import com.direwolf20.charginggadgets.common.items.ChargingStationItem;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChargingGadgets.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ChargingGadgets.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, ChargingGadgets.MOD_ID);

    public static final RegistryObject<Block> CHARGING_STATION = BLOCKS.register("charging_station", ChargingStationBlock::new);

    /**
     * Tile Entities
     */
    public static final RegistryObject<TileEntityType<ChargingStationTile>> CHARGING_STATION_TILE =
            TILES_ENTITIES.register("charging_station_tile", () -> TileEntityType.Builder.create(ChargingStationTile::new, CHARGING_STATION.get()).build(null));

    /**
     * Containers?
     */
    public static final RegistryObject<ContainerType<ChargingStationContainer>> CHARGING_STATION_CONTAINER = CONTAINERS.register("charging_station_container", () -> IForgeContainerType.create(ChargingStationContainer::new));

    /**
     * For now I'm adding items into here, it doesn't make much sense but nor does an items package for a mod with no
     * items... so... when we add items. Move this!
     */
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ChargingGadgets.MOD_ID);
    public static final RegistryObject<Item> CHARGING_STATION_BI = ITEMS.register("charging_station", () -> new ChargingStationItem(CHARGING_STATION.get(), ChargingGadgets.ITEM_PROPS));
}
