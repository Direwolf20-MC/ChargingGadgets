package com.direwolf20.charginggadgets.common.blocks;

import com.direwolf20.charginggadgets.ChargingGadgets;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChargingGadgets.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ChargingGadgets.MOD_ID);

    public static final RegistryObject<Block> CHARGING_STATION = BLOCKS.register("charging_station", ChargingStationBlock::new);

    /**
     * Tile Entities
     */
    public static final RegistryObject<TileEntityType<ChargingStationTile>> CHARGING_STATION_TILE =
            TILES_ENTITIES.register("charging_station_tile", () -> TileEntityType.Builder.create(ChargingStationTile::new, CHARGING_STATION.get()).build(null));

}
