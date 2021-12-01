package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.common.ChargingGadgets;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChargingGadgets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Generators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            generator.addProvider(new GeneratorRecipes(generator));
            generator.addProvider(new GeneratorLoots(generator));
        }

        if (event.includeClient()) {
            generator.addProvider(new GeneratorLanguage(generator));
            generator.addProvider(new GeneratorBlockStates(generator, event.getExistingFileHelper()));
            generator.addProvider(new GeneratorItemModels(generator, event.getExistingFileHelper()));
        }
    }

}

