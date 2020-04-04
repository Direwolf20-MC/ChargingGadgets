package com.direwolf20.charginggadgets.common.tiles;

import com.direwolf20.charginggadgets.common.Config;
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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Todo: completely rewrite this class from the ground up
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

    private int counter = 0;
    private int maxBurn = 0;

    private LazyOptional<ChargerEnergyStorage> energy = LazyOptional.of(() -> new ChargerEnergyStorage(this, 0, Config.GENERAL.chargerMaxPower.get()));
    private LazyOptional<ItemStackHandler> inventory  = LazyOptional.of(() -> new ChargerItemHandler(this));

    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final IIntArray chargingStationData = new IIntArray() {
        @Override
        public int get(int index) {
            if( index == 0 )
                return ChargingStationTile.this.energy.map(ChargerEnergyStorage::getEnergyStored).orElse(0);
            if( index == 1 )
                return ChargingStationTile.this.energy.map(ChargerEnergyStorage::getMaxEnergyStored).orElse(0);
            if( index == 2 )
                return ChargingStationTile.this.counter;
            if( index == 3 )
                return ChargingStationTile.this.maxBurn;

            return 0;
        }

        // Not actually sure if we should be setting anything?
        @Override
        public void set(int index, int value) {
            if( index == 0 )
                ChargingStationTile.this.getEnergyStorage().receiveEnergy(value, false);
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ChargingStationTile() {
        super(ModBlocks.CHARGING_STATION_TILE.get());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        return new ChargingStationContainer(this, this.chargingStationData, i, playerInventory, this.inventory.orElse(new ItemStackHandler(2)));
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
    }

    private void chargeItem(ItemStack stack) {
        this.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyStorage -> stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(itemEnergy -> {
            if (!isChargingItem(itemEnergy))
                return;

            int energyRemoved = itemEnergy.receiveEnergy(Math.min(energyStorage.getEnergyStored(), 2500), false);
            ((ChargerEnergyStorage) energyStorage).consumeEnergy(energyRemoved, false);
        }));
    }

    public boolean isChargingItem(IEnergyStorage energy) {
        return energy.getEnergyStored() >= 0 && energy.receiveEnergy(energy.getEnergyStored(), true) >= 0;
    }

    private void tryBurn() {
        if( world == null )
            return;


        this.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyStorage -> {
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
        energyStorage.receiveEnergy(2500, false);

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
        super.read(compound);

        inventory.ifPresent(h -> h.deserializeNBT(compound.getCompound("inv")));
        energy.ifPresent(h -> h.deserializeNBT(compound.getCompound("energy")));
        counter = compound.getInt("counter");
        maxBurn = compound.getInt("maxburn");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        inventory.ifPresent(h ->  compound.put("inv", h.serializeNBT()));
        energy.ifPresent(h -> compound.put("energy", h.serializeNBT()));

        compound.putInt("counter", counter);
        compound.putInt("maxburn", maxBurn);
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

    public IEnergyStorage getEnergyStorage() {
        return this.getCapability(CapabilityEnergy.ENERGY).orElse(new EnergyStorage(0));
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        read(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
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
}
