package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.direwolf20.charginggadgets.capabilities.ChargerItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationContainer extends AbstractContainerMenu {
    private static final int SLOTS = 2;

    public final ContainerData data;
    public ChargerItemHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    private ChargingStationTile tile;

    public ChargingStationContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this((ChargingStationTile) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4), windowId, playerInventory, new ChargerItemHandler(null));
    }

    public ChargingStationContainer(@Nullable ChargingStationTile tile, ContainerData chargingStationData, int windowId, Inventory playerInventory, ChargerItemHandler handler) {
        super(BlockRegistry.CHARGING_STATION_CONTAINER.get(), windowId);

        this.handler = handler;
        this.tile = tile;

        this.data = chargingStationData;
        this.setup(playerInventory);

        addDataSlots(chargingStationData);
    }

    public void setup(Inventory inventory) {
        addSlot(new ResourceHandlerSlot(handler, handler::set, 0, 65, 43));
        addSlot(new ResourceHandlerSlot(handler, handler::set, 1, 119, 43));

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 56 + 86;
            addSlot(new Slot(inventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++row) {
            for (int col = 0; col < 9; ++col) {
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
                // Shift-clicking out of the machine slots into player inventory
                if (!this.moveItemStackTo(currentStack, SLOTS, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Shift-clicking from player inventory into the machine
                // Route to the correct slot based on item type
                boolean moved = false;

                if (ItemAccess.forStack(currentStack).getCapability(Capabilities.Energy.ITEM) != null) {
                    // Item accepts energy — send to charge slot (slot 1)
                    moved = this.moveItemStackTo(currentStack, ChargingStationTile.Slots.CHARGE.getId(), ChargingStationTile.Slots.CHARGE.getId() + 1, false);
                }

                if (!moved && (currentStack.getItem() == Items.BUCKET
                        || currentStack.getBurnTime(RecipeType.SMELTING, playerIn.level().fuelValues()) > 0)) {
                    // Item is burnable or a bucket — send to fuel slot (slot 0)
                    moved = this.moveItemStackTo(currentStack, ChargingStationTile.Slots.FUEL.getId(), ChargingStationTile.Slots.FUEL.getId() + 1, false);
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
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
}
