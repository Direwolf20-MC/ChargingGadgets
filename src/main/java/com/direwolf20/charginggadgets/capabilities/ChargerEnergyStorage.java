package com.direwolf20.charginggadgets.capabilities;

import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationTile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;


public class ChargerEnergyStorage extends SimpleEnergyHandler {
    private static final String KEY = "energy";
    private final ChargingStationTile tile;

    public ChargerEnergyStorage(ChargingStationTile tile, int energy, int capacity) {
        super(capacity, capacity, 0, energy); // maxInsert = capacity (internal use), maxExtract = 0 (no external extraction)
        this.tile = tile;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        tile.setChanged();
    }

    /**
     * Internal method to consume energy for charging items.
     * Since we set maxExtract=0, external extraction is blocked,
     * but we need this for internal use.
     */
    public int consumeEnergy(int amount, boolean simulate) {
        int consumed = Math.min(getAmountAsInt(), amount);
        if (!simulate && consumed > 0) {
            set(getAmountAsInt() - consumed);
        }
        return consumed;
    }

    /**
     * Convenience method for internal insert (e.g. from fuel burning).
     * Returns the amount actually inserted.
     */
    public int addEnergy(int amount, boolean simulate) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = insert(amount, tx);
            if (!simulate) tx.commit();
            return inserted;
        }
    }

    public void serialize(ValueOutput output) {
        output.putInt(KEY, getAmountAsInt());
    }

    public void deserialize(ValueInput input) {
        set(input.getIntOr(KEY, 0));
    }

    @Override
    public String toString() {
        return "ChargerEnergyStorage{" +
                "energy=" + getAmountAsInt() +
                ", capacity=" + getCapacityAsInt() +
                '}';
    }
}
