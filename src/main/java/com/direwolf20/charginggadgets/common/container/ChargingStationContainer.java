package com.direwolf20.charginggadgets.common.container;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationContainer extends Container {
    private static final int SLOTS = 2;

    public final IIntArray data;
    public ItemStackHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sidess
    private ChargingStationTile tile;

    public ChargingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this((ChargingStationTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), new IntArray(4), windowId, playerInventory, new ItemStackHandler(2));
    }

    public ChargingStationContainer(@Nullable ChargingStationTile tile, IIntArray chargingStationData, int windowId, PlayerInventory playerInventory, ItemStackHandler handler) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);

        this.handler = handler;
        this.tile = tile;

        this.data = chargingStationData;
        this.setup(playerInventory);

        trackIntArray(chargingStationData);
    }

    public void setup(PlayerInventory inventory) {
        addSlot(new RestrictedSlot(handler, 0, 65, 43));
        addSlot(new RestrictedSlot(handler, 1, 119, 43));

        // Slots for the hotbar
        for (int row = 0; row < 9; ++ row) {
            int x = 8 + row * 18;
            int y = 56 + 86;
            addSlot(new Slot(inventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                int x = 8 + col * 18;
                int y = row * 18 + (56 + 10);
                addSlot(new Slot(inventory, col + row * 9, x, y));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (! this.mergeItemStack(currentStack, SLOTS, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.mergeItemStack(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vec3d(this.tile.getPos()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    public int getMaxPower() {
        return this.data.get(1) * 32;
    }

    public int getEnergy() {
        return this.data.get(0) * 32;
    }

    public int getMaxBurn() {
        return this.data.get(3);
    }

    public int getRemaining() {
        return this.data.get(2);
    }

    static class RestrictedSlot extends SlotItemHandler {
        public RestrictedSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            if( getSlotIndex() == ChargingStationTile.Slots.CHARGE.getId() )
                return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();

            if( getSlotIndex() == ChargingStationTile.Slots.FUEL.getId() )
                return ForgeHooks.getBurnTime(stack) != 0;

            return super.isItemValid(stack);
        }
    }
}
