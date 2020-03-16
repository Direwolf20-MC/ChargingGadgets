package com.direwolf20.charginggadgets.common.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class ItemEnergyStorage extends EnergyStorage {
    private final ItemStack stack;
    private static final String KEY = "energy";

    public ItemEnergyStorage(ItemStack stack, int capacity) {
        super(capacity, Integer.MAX_VALUE);
        this.stack = stack;
    }

    // We don't want other blocks or things extracting our energy
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public int internalExtractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);

        if( !simulate ) {
            CompoundNBT nbt = stack.getOrCreateTag();
            nbt.putInt(KEY, this.energy);
        }

        return extracted;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if( !simulate ) {
            CompoundNBT nbt = stack.getOrCreateTag();
            nbt.putInt(KEY, this.energy);
        }

        return received;
    }
}
