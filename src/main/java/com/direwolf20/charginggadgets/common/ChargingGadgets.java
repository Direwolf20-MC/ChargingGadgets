package com.direwolf20.charginggadgets.common;

import com.direwolf20.charginggadgets.client.screens.ChargingStationScreen;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ChargingGadgets.MOD_ID)
public class ChargingGadgets
{
    public static final String MOD_ID = "charginggadgets";
    private static final Logger LOGGER = LogManager.getLogger();

    // Item Groups
    public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.CHARGING_STATION.get());
        }
    };

    public static final Item.Properties ITEM_PROPS = new Item.Properties().group(ITEM_GROUP);

    public ChargingGadgets() {
        IEventBus event = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.ITEMS.register(event);
        ModBlocks.BLOCKS.register(event);
        ModBlocks.TILES_ENTITIES.register(event);
        ModBlocks.CONTAINERS.register(event);

        event.addListener(this::setup);
        event.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModBlocks.CHARGING_STATION_CONTAINER.get(), ChargingStationScreen::new);
        // need the render
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(e -> {
//            ClientRegistry.bindTileEntityRenderer(ChargingStationTile.TYPE, ChargingStationTER::new);
//        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
