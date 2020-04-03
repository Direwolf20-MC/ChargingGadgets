package com.direwolf20.charginggadgets.common.capabilities;

import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class ChargerEnergyStorage implements IEnergyStorage, INBTSerializable<CompoundNBT> {
    private static final String KEY = "energy";
    private int energy;
    private int capacity;
    private int maxInOut = 1000000;
    private ChargingStationTile tile;

    public ChargerEnergyStorage(ChargingStationTile tile,int energy, int capacity) {
        this.energy = energy;
        this.capacity = capacity;
        this.tile = tile;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(KEY, this.energy);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.energy = nbt.getInt(KEY);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxInOut, maxReceive));

        if (!simulate) {
            energy += energyReceived;
            this.tile.markDirty();
        }

        return energyReceived;
    }

    public int consumeEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(this.maxInOut, maxExtract));

        if (!simulate)
            energy -= energyExtracted;

        return energyExtracted;
    }

    // We don't use this method and thus we don't let other people use it either
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public String toString() {
        return "ChargerEnergyStorage{" +
                "energy=" + energy +
                ", capacity=" + capacity +
                ", maxInOut=" + maxInOut +
                '}';
    }
}
