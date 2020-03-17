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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ChargingStationContainer extends Container {
    private static final int SLOTS = 2;

    private ChargingStationTile tile;

    public ChargingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);
        BlockPos pos = extraData.readBlockPos();
        this.tile = (ChargingStationTile) playerInventory.player.world.getTileEntity(pos);
        this.setup(playerInventory);
    }

    public ChargingStationContainer(ChargingStationTile tile, int windowId, PlayerInventory playerInventory) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);
        this.tile = tile;
        this.setup(playerInventory);
    }

    public void setup(PlayerInventory inventory) {
        this.getTile().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> {
            int y = 43;

            addSlot(new RestrictedSlot(handler, 0, 65, y));
            addSlot(new RestrictedSlot(handler, 1, 119, y));
        });

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
        return !getTile().isRemoved() && playerIn.getDistanceSq(new Vec3d(getTile().getPos()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    public ChargingStationTile getTile() {
        return tile;
    }

    public int getEnergy() {
        return tile.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
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
