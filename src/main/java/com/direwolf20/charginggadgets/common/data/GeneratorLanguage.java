package com.direwolf20.charginggadgets.common.data;

import com.direwolf20.charginggadgets.common.ChargingGadgets;
import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class GeneratorLanguage extends LanguageProvider {
    public GeneratorLanguage(DataGenerator gen) {
        super(gen, ChargingGadgets.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModBlocks.CHARGING_STATION, "Charging Station");
        add("itemGroup.charginggadgets", "Charging Gadgets");
        add("screen.charginggadgets.energy", "Energy: %s/%s FE");
        add("screen.charginggadgets.no_fuel", "Fuel source empty");
        add("screen.charginggadgets.burn_time", "Burn time left: %ss");
    }
}
