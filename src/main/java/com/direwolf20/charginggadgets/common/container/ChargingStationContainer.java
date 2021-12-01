package com.direwolf20.charginggadgets.common.container;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationContainer extends AbstractContainerMenu {
    private static final int SLOTS = 2;

    public final ContainerData data;
    public ItemStackHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    private ChargingStationTile tile;

    public ChargingStationContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this((ChargingStationTile) playerInventory.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4), windowId, playerInventory, new ItemStackHandler(2));
    }

    public ChargingStationContainer(@Nullable ChargingStationTile tile, ContainerData chargingStationData, int windowId, Inventory playerInventory, ItemStackHandler handler) {
        super(ModBlocks.CHARGING_STATION_CONTAINER.get(), windowId);

        this.handler = handler;
        this.tile = tile;

        this.data = chargingStationData;
        this.setup(playerInventory);

        addDataSlots(chargingStationData);
    }

    public void setup(Inventory inventory) {
        addSlot(new RestrictedSlot(handler, 0, 65, 43));
        addSlot(new RestrictedSlot(handler, 1, 119, 43));

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
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
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (!this.moveItemStackTo(currentStack, SLOTS, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        BlockPos pos = this.tile.getBlockPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.distanceToSqr(new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
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
        public boolean mayPlace(@Nonnull ItemStack stack) {
            if (getSlotIndex() == ChargingStationTile.Slots.CHARGE.getId())
                return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();

            if (getSlotIndex() == ChargingStationTile.Slots.FUEL.getId())
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) != 0;

            return super.mayPlace(stack);
        }
    }
}
