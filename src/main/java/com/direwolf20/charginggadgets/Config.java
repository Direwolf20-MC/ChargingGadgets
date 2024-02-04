package com.direwolf20.charginggadgets;


import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static ModConfigSpec SERVER_CONFIG;

    public static final class CategoryGeneral {
        public final ModConfigSpec.IntValue chargerMaxPower;

        private CategoryGeneral() {
            SERVER_BUILDER.comment("General settings").push("general");

            chargerMaxPower = SERVER_BUILDER.comment("Maximum power for the Charging Station")
                    .defineInRange("chargerMaxEnergy", 1000000, 0, Integer.MAX_VALUE);

            SERVER_BUILDER.pop();
            SERVER_CONFIG = SERVER_BUILDER.build();
        }
    }
}