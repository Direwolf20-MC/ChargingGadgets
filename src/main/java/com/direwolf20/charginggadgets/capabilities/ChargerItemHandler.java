package com.direwolf20.charginggadgets.capabilities;

import com.direwolf20.charginggadgets.blocks.chargingstation.ChargingStationTile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;

public class ChargerItemHandler extends ItemStacksResourceHandler {
    private final ChargingStationTile chargingStationTile;

    public ChargerItemHandler(ChargingStationTile chargingStationTile) {
        super(2);
        this.chargingStationTile = chargingStationTile;
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previous) {
        if (chargingStationTile != null)
            chargingStationTile.setChanged();
    }

    @Override
    public boolean isValid(int index, @Nonnull ItemResource resource) {
        // Client-side dummy handler has no tile — allow everything, server enforces rules
        if (chargingStationTile == null)
            return true;

        ItemStack stack = resource.toStack(1);

        if (index == ChargingStationTile.Slots.FUEL.getId() && stack.getItem() == Items.BUCKET)
            return true;

        if (index == ChargingStationTile.Slots.FUEL.getId()) {
            return stack.getBurnTime(RecipeType.SMELTING, chargingStationTile.getFuelValues()) > 0;
        }

        if (index == ChargingStationTile.Slots.CHARGE.getId()) {
            return ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM) != null;
        }

        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        // Client-side dummy handler has no tile — allow everything, server enforces rules
        if (chargingStationTile == null)
            return super.insert(index, resource, amount, transaction);

        ItemStack stack = resource.toStack(1);

        if (index == ChargingStationTile.Slots.FUEL.getId() && stack.getItem() == Items.BUCKET)
            return super.insert(index, resource, amount, transaction);

        if (index == ChargingStationTile.Slots.FUEL.getId() && stack.getBurnTime(RecipeType.SMELTING, chargingStationTile.getFuelValues()) <= 0)
            return 0;

        if (index == ChargingStationTile.Slots.CHARGE.getId() && (ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM) == null || getAmountAsInt(index) > 0))
            return 0;

        return super.insert(index, resource, amount, transaction);
    }
}
