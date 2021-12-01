package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationScreen;
import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.NotNull;

@Mod(ChargingGadgets.MOD_ID)
public class ChargingGadgets
{
    public static final String MOD_ID = "charginggadgets";
    private static final Logger LOGGER = LogManager.getLogger();

    // Item Groups
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(BlockRegistry.CHARGING_STATION.get());
        }
    };

    public static final Item.Properties ITEM_PROPS = new Item.Properties().tab(ITEM_GROUP);

    public ChargingGadgets() {
        IEventBus event = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.ITEMS.register(event);
        BlockRegistry.BLOCKS.register(event);
        BlockRegistry.TILES_ENTITIES.register(event);
        BlockRegistry.CONTAINERS.register(event);

        event.addListener(this::setup);
        event.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(BlockRegistry.CHARGING_STATION_CONTAINER.get(), ChargingStationScreen::new);
    }
}
