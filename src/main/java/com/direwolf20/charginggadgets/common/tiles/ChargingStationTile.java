package com.direwolf20.charginggadgets.common.tiles;

import com.direwolf20.charginggadgets.common.blocks.ModBlocks;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private enum Slots {
        FUEL(0),
        CHARGE(1);

        int id;

        Slots(int number) {
            id = number;
        }
    }

    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(this::createEnergy);
    private LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(this::createHandler);
    private ChargerItemHandler itemHandler;
    private EnergyStorage energy;
    private static final int FUEL_SLOT = 0;
    private static final int CHARGE_SLOT = 1;
    private int counter = 0;
    private int maxBurn = 0;

    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());

        itemHandler = new ChargerItemHandler();
        itemHandlerCapability = LazyOptional.of(() -> itemHandler);
        energy = new EnergyStorage(1500000);
        energyCapability = LazyOptional.of(() -> energy);
    }

    private IItemHandler createHandler() {
        itemHandler = new ChargerItemHandler();
        return itemHandler;
    }

    private IEnergyStorage createEnergy() {
        energy = new EnergyStorage(1500000);
        return energy;

    }

    @Override
    public void onLoad() {
        if (!itemHandlerCapability.isPresent())
            itemHandlerCapability = LazyOptional.of(() -> itemHandler);

        if (!energyCapability.isPresent())
            energyCapability = LazyOptional.of(() -> energy);
    }

    @Override
    public void onChunkUnloaded() {
        itemHandlerCapability.invalidate();
        energyCapability.invalidate();
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

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ChargingStationContainer(i, playerEntity.world, this.pos, playerInventory, playerEntity);
    }

    @Override
    public void tick() {
        if (getWorld() != null) {
            tryBurn();

            ItemStack stack = getChargeStack();
            if (!stack.isEmpty())
                chargeItem(stack);

            //todo AT the cached BlockState, so that we can reset it if necessary

        } else if (getWorld() != null) {
            tryBurn();

            ItemStack stack = getChargeStack();
            if (!stack.isEmpty()) {
                chargeItem(stack);
                //updateLightning();
            }
        }
        //getEnergy().resetReceiveCap();
    }

    @Nonnull
    private EnergyStorage getEnergy() {
        return energy;
    }

    private void chargeItem(ItemStack stack) {
        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(chargingStorage -> {
            if (isChargingItem(chargingStorage))
                addEnergy(chargingStorage.receiveEnergy(Math.min(getEnergy().getEnergyStored(), 2500), false) * -1); //I know it looks stupid it just didn't work any other way lol
        });
    }

    public boolean isChargingItem(IEnergyStorage energy) {
        return getEnergy().getEnergyStored() > 0 && energy.receiveEnergy(getEnergy().getEnergyStored(), true) > 0;
    }

    private void tryBurn() {
        assert getWorld() != null;
        boolean canInsertEnergy = getEnergy().receiveEnergy(2500, true) > 0;
        if (counter > 0 && canInsertEnergy) {
            burn();
        } else if (canInsertEnergy) {
            if (initBurn())
                burn();
        }
    }

    private void addEnergy(int amount) {
        energy.receiveEnergy(amount, false);
    }

    private void burn() {
        addEnergy(2500);
        counter--;
        if (counter == 0) {
            maxBurn = 0;
            initBurn();
        }
    }

    private boolean initBurn() {
        ItemStack stack = getFuelStack();
        int burnTime = ForgeHooks.getBurnTime(stack);
        if (burnTime > 0) {
            getItemStackHandler().extractItem(0, 1, false);
            counter = (int) Math.floor(burnTime) / 50;
            maxBurn = counter;
            return true;
        }
        return false;
    }

    @Nonnull
    private ItemStackHandler getItemStackHandler() {
        return itemHandler;
    }

    private ItemStack getChargeStack() {
        return getItemStackHandler().getStackInSlot(CHARGE_SLOT);
    }

    public ItemStack getFuelStack() {
        return getItemStackHandler().getStackInSlot(FUEL_SLOT);
    }

    @Override
    public void read(CompoundNBT compound) {
        CompoundNBT invTag = compound.getCompound("inv");
        itemHandlerCapability.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        CompoundNBT energyTag = compound.getCompound("energy");
        energyCapability.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(energyTag));
        counter = compound.getInt("counter");
        maxBurn = compound.getInt("maxburn");
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        itemHandlerCapability.ifPresent(h -> {
            CompoundNBT compoundItem = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            compound.put("inv", compoundItem);
        });
        energyCapability.ifPresent(h -> {
            CompoundNBT compoundEnergy = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            compound.put("energy", compoundEnergy);
        });
        compound.putInt("counter", counter);
        compound.putInt("maxburn", maxBurn);
        return super.write(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return itemHandlerCapability.cast();

        if (cap == CapabilityEnergy.ENERGY)
            return energyCapability.cast();

        return super.getCapability(cap, side);
    }

    class ChargerItemHandler extends ItemStackHandler {
        public ChargerItemHandler() {
            super(2);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ChargingStationTile.this.markDirty();
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot == Slots.FUEL.id && ForgeHooks.getBurnTime(stack) <= 0)
                return stack;

            if (slot == Slots.CHARGE.id && (! stack.getCapability(CapabilityEnergy.ENERGY).isPresent() || getStackInSlot(slot).getCount() > 0))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    }
}
