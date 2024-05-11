package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChargingGadgets.MOD_ID)
public class ChargingGadgets {
    public static final String MOD_ID = "charginggadgets";
    private static final Logger LOGGER = LogManager.getLogger();

    public ChargingGadgets(IEventBus eventBus, ModContainer container) {
        //IEventBus event = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.ITEMS.register(eventBus);
        BlockRegistry.BLOCKS.register(eventBus);
        BlockRegistry.TILES_ENTITIES.register(eventBus);
        BlockRegistry.CONTAINERS.register(eventBus);
        CGDataComponents.COMPONENTS.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::setupCreativeTabs);

        //MinecraftForge.EVENT_BUS.register(this);

        container.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {

    }

    private void setupCreativeTabs(final RegisterEvent event) {
        ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "creative_tab"));
        event.register(Registries.CREATIVE_MODE_TAB, creativeModeTabRegisterHelper ->
        {
            creativeModeTabRegisterHelper.register(TAB, CreativeModeTab.builder().icon(() -> new ItemStack(BlockRegistry.CHARGING_STATION_BI.get()))
                    .title(Component.translatable("itemGroup." + MOD_ID))
                    .displayItems((params, output) -> {
                        output.accept(BlockRegistry.CHARGING_STATION_BI.get());
                    })
                    .build());
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> ((ChargingStationTile) be).inventory,
                // blocks to register for
                BlockRegistry.CHARGING_STATION.get());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> ((ChargingStationTile) be).energyStorage,
                // blocks to register for
                BlockRegistry.CHARGING_STATION.get());
    }
}
