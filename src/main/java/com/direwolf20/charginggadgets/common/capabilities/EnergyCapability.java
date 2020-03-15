package com.direwolf20.charginggadgets.common.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyCapability extends EnergyStorage {
    private static final String KEY = "energy";
    private ItemStack stack;
    private boolean canExtract;

    public EnergyCapability(ItemStack stack, int capacity) {
        this(stack, capacity, true);
    }

    public EnergyCapability(ItemStack stack, int capacity, boolean canExtract) {
        super(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.canExtract = canExtract;

        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return super.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if( !canExtract() )
            return 0;

        return super.extractEnergy(maxExtract, simulate);
    }

    public boolean canExtract() {
        return this.canExtract;
    }
}
