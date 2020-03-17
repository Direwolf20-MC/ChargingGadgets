package com.direwolf20.charginggadgets.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static ForgeConfigSpec COMMON_CONFIG;

    public static final class CategoryGeneral {
        public final ForgeConfigSpec.IntValue chargerMaxPower;

        private CategoryGeneral() {
            COMMON_BUILDER.comment("General settings").push("general");

            chargerMaxPower = COMMON_BUILDER.comment("Maximum power for the Charging Station")
                    .defineInRange("chargerMaxEnergy", 1000000, 0, Integer.MAX_VALUE);

            COMMON_BUILDER.pop();
            COMMON_CONFIG = COMMON_BUILDER.build();
        }
    }
}