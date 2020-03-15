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
        Slots(int number) { id = number; }
    }

    private LazyOptional<IEnergyStorage> energyCapability;
    private LazyOptional<IItemHandler> itemHandlerCapability;
    private ChargerItemHandler itemHandler;
    private EnergyStorage energy;

    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());

        itemHandler = new ChargerItemHandler();
        itemHandlerCapability = LazyOptional.of(() -> itemHandler);
        energy = new EnergyStorage(1500000){
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }
        };

        energyCapability = LazyOptional.of(() -> energy);
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

    public int getRemainingBurn() {
        return 0;
    }

    public int getMaxBurn() {
        return 0;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Charging Station Tile");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ChargingStationContainer(i, playerEntity.world, this.pos, playerInventory, playerEntity);
    }

    @Override
    public void tick() {
//        energy.receiveEnergy(1, false);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        if( compound.contains("items") )
            itemHandler.deserializeNBT(compound.getCompound("items"));

        if( compound.contains("energy") )
            energy.receiveEnergy(compound.getInt("energy"), false);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("items", itemHandler.serializeNBT());
        compound.putInt("energy", energy.getEnergyStored());
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
