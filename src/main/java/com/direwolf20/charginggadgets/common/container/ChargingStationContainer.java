package com.direwolf20.charginggadgets.common.container;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ChargingStationContainer extends Container {
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

            addSlot(new SlotItemHandler(handler, 0, 65, y));
            addSlot(new SlotItemHandler(handler, 1, 119, y));
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
        System.out.println(index);
        return ItemStack.EMPTY;
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
}
