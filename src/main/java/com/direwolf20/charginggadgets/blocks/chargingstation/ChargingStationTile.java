package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.Config;
import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.direwolf20.charginggadgets.capabilities.ChargerEnergyStorage;
import com.direwolf20.charginggadgets.capabilities.ChargerItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Todo: completely rewrite this class from the ground up
public class ChargingStationTile extends BlockEntity implements MenuProvider {
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

    public ChargerEnergyStorage energyStorage;
    private LazyOptional<ChargerEnergyStorage> energy;
    private LazyOptional<ItemStackHandler> inventory  = LazyOptional.of(() -> new ChargerItemHandler(this));

    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final ContainerData chargingStationData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ChargingStationTile.this.energyStorage.getEnergyStored() / 32;
                case 1 -> ChargingStationTile.this.energyStorage.getMaxEnergyStored() / 32;
                case 2 -> ChargingStationTile.this.counter;
                case 3 -> ChargingStationTile.this.maxBurn;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public ChargingStationTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.CHARGING_STATION_TILE.get(), pos, state);
        this.energyStorage = new ChargerEnergyStorage(this, 0, Config.GENERAL.chargerMaxPower.get());
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        assert level != null;
        return new ChargingStationContainer(this, this.chargingStationData, i, playerInventory, this.inventory.orElse(new ItemStackHandler(2)));
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos blockPos, BlockState state, T t) {
        if (t instanceof ChargingStationTile entity) {
            entity.inventory.ifPresent(handler -> {
                entity.tryBurn();

                ItemStack stack = handler.getStackInSlot(Slots.CHARGE.id);
                if (!stack.isEmpty())
                    entity.chargeItem(stack);
            });
        }
    }


    private void chargeItem(ItemStack stack) {
        this.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(itemEnergy -> {
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
        if (level == null)
            return;

        this.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            boolean canInsertEnergy = energyStorage.receiveEnergy(625, true) > 0;
            if (counter > 0 && canInsertEnergy) {
                burn(energyStorage);
            } else if (canInsertEnergy) {
                if (initBurn())
                    burn(energyStorage);
            }
        });
    }


    private void burn(IEnergyStorage energyStorage) {
        energyStorage.receiveEnergy(625, false);

        counter--;
        if (counter == 0) {
            maxBurn = 0;
            initBurn();
        }
    }

    private boolean initBurn() {
        ItemStackHandler handler = inventory.orElseThrow(RuntimeException::new);
        ItemStack stack = handler.getStackInSlot(Slots.FUEL.id);

        int burnTime = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
        if (burnTime > 0) {
            Item fuelStack = handler.getStackInSlot(Slots.FUEL.id).getItem();
            handler.extractItem(0, 1, false);
            if( fuelStack instanceof BucketItem && fuelStack != Items.BUCKET )
                handler.insertItem(0, new ItemStack(Items.BUCKET, 1), false);

            setChanged();
            counter = (int) Math.floor(burnTime) / 50;
            maxBurn = counter;
            return true;
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        inventory.ifPresent(h -> h.deserializeNBT(compound.getCompound("inv")));
        energy.ifPresent(h -> h.deserializeNBT(compound.getCompound("energy")));
        counter = compound.getInt("counter");
        maxBurn = compound.getInt("maxburn");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        inventory.ifPresent(h -> compound.put("inv", h.serializeNBT()));
        energy.ifPresent(h -> compound.put("energy", h.serializeNBT()));

        compound.putInt("counter", counter);
        compound.putInt("maxburn", maxBurn);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return inventory.cast();

        if (cap == ForgeCapabilities.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this, entity -> this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        saveAdditional(compoundTag);
        return compoundTag;
    }


    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public void setRemoved() {
        energy.invalidate();
        inventory.invalidate();
        super.setRemoved();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Charging Station Tile");
    }
}
