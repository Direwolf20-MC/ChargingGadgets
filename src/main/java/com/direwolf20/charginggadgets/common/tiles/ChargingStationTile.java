package com.direwolf20.charginggadgets.common.tiles;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
import com.direwolf20.charginggadgets.common.capabilities.ChargerEnergyStorage;
import com.direwolf20.charginggadgets.common.capabilities.ChargerItemHandler;
import com.direwolf20.charginggadgets.common.container.ChargingStationContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public enum Slots {
        FUEL(0),
        CHARGE(1);

        int id;

        Slots(int number) {
            id = number;
        }

        public int getId() {
            return id;
        }
    }

    private LazyOptional<ChargerEnergyStorage> energy = LazyOptional.of(() -> new ChargerEnergyStorage(this, 1500000));
    private LazyOptional<ItemStackHandler> inventory  = LazyOptional.of(() -> new ChargerItemHandler(this));

    private int counter = 0;
    private int maxBurn = 0;

    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());
    }

//    @Override
//    public void onLoad() {
////        if (!itemHandlerCapability.isPresent())
////            itemHandlerCapability = LazyOptional.of(() -> itemHandler);
////
////        if (!energyCapability.isPresent())
////            energyCapability = LazyOptional.of(() -> energy);
//    }
//
//    @Override
//    public void onChunkUnloaded() {
////        itemHandlerCapability.invalidate();
////        energyCapability.invalidate();
//    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ChargingStationContainer(i, playerEntity.world, this.pos, playerInventory, playerEntity);
    }

    @Override
    public void tick() {
        if (getWorld() == null)
            return;

        inventory.ifPresent(handler -> {
            tryBurn();

            ItemStack stack = handler.getStackInSlot(Slots.CHARGE.id);
            if (!stack.isEmpty())
                chargeItem(stack);
        });
//        getEnergy().resetReceiveCap();
    }

    private void chargeItem(ItemStack stack) {
        energy.ifPresent(energyStorage -> stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(itemEnergy -> {
            if (!isChargingItem(itemEnergy))
                return;

            int energyRemoved = itemEnergy.receiveEnergy(Math.min(energyStorage.getEnergyStored(), 2500), false);
            energyStorage.internalExtractEnergy(energyRemoved, false);
        }));
    }

    public boolean isChargingItem(IEnergyStorage energy) {
        return energy.getEnergyStored() >= 0 && energy.receiveEnergy(energy.getEnergyStored(), true) >= 0;
    }

    private void tryBurn() {
        if( world == null )
            return;

        energy.ifPresent(energyStorage -> {
            boolean canInsertEnergy = energyStorage.receiveEnergy(2500, true) > 0;
            if (counter > 0 && canInsertEnergy) {
                burn(energyStorage);
            } else if (canInsertEnergy) {
                if (initBurn())
                    burn(energyStorage);
            }
        });
    }


    private void burn(IEnergyStorage energyStorage) {
        System.out.println(energyStorage.receiveEnergy(2500, false));;

        counter--;
        if (counter == 0) {
            maxBurn = 0;
            initBurn();
        }
    }

    private boolean initBurn() {
        ItemStackHandler handler = inventory.orElseThrow(RuntimeException::new);
        ItemStack stack = handler.getStackInSlot(Slots.FUEL.id);

        int burnTime = ForgeHooks.getBurnTime(stack);
        if (burnTime > 0) {
            handler.extractItem(0, 1, false);
            markDirty();
            counter = (int) Math.floor(burnTime) / 50;
            maxBurn = counter;
            return true;
        }
        return false;
    }

    @Override
    public void read(CompoundNBT compound) {
        CompoundNBT invTag = compound.getCompound("inv");
        inventory.ifPresent(h -> h.deserializeNBT(invTag));
        CompoundNBT energyTag = compound.getCompound("energy");
        energy.ifPresent(h -> h.deserializeNBT(energyTag));
        counter = compound.getInt("counter");
        maxBurn = compound.getInt("maxburn");
        System.out.println(compound);
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        inventory.ifPresent(h ->  compound.put("inv", h.serializeNBT()));
        energy.ifPresent(h -> compound.put("energy", h.serializeNBT()));

        compound.putInt("counter", counter);
        compound.putInt("maxburn", maxBurn);
        System.out.println(compound);
        return super.write(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return inventory.cast();

        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        energy.invalidate();
        inventory.invalidate();
        super.remove();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Charging Station Tile");
    }


    public int getRemainingBurn() {
        return counter;
    }

    public int getMaxBurn() {
        return maxBurn;
    }
}
