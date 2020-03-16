package com.direwolf20.charginggadgets.common.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ItemHandlerWrapper implements IInventory {
    @Nonnull
    private final IItemHandlerModifiable handler;

    public ItemHandlerWrapper(@Nonnull IItemHandlerModifiable handler) {
        this.handler = Objects.requireNonNull(handler, "Cannot construct an ItemHandlerWrapper without an Inventory to wrap.");
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return getSizeInventory() <= 0;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        return handler.extractItem(index, count, false);
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return handler.extractItem(index, getStackInSlot(index).getCount(), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) { return true; }

    @Override
    public void clear() {
        for (int i = 0; i < getSizeInventory(); i++) {
            setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }
}