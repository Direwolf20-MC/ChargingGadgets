package com.direwolf20.charginggadgets;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChargingGadgets.MOD_ID)
public class ChargingGadgets
{
    public static final String MOD_ID = "charginggadgets";
    private static final Logger LOGGER = LogManager.getLogger();

    public ChargingGadgets() {
        IEventBus event = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.ITEMS.register(event);
        BlockRegistry.BLOCKS.register(event);
        BlockRegistry.TILES_ENTITIES.register(event);
        BlockRegistry.CONTAINERS.register(event);

        event.addListener(this::setup);
        event.addListener(this::clientSetup);
        event.addListener(this::setupCreativeTabs);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(BlockRegistry.CHARGING_STATION_CONTAINER.get(), ChargingStationScreen::new);
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
}
