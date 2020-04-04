package com.direwolf20.charginggadgets.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static ForgeConfigSpec SERVER_CONFIG;

    public static final class CategoryGeneral {
        public final ForgeConfigSpec.IntValue chargerMaxPower;

        private CategoryGeneral() {
            SERVER_BUILDER.comment("General settings").push("general");

            chargerMaxPower = SERVER_BUILDER.comment("Maximum power for the Charging Station")
                    .defineInRange("chargerMaxEnergy", 1000000, 0, Integer.MAX_VALUE);

            SERVER_BUILDER.pop();
            SERVER_CONFIG = SERVER_BUILDER.build();
        }
    }
}