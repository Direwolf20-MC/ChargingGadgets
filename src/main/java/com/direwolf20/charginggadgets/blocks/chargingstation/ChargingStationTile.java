package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.Config;
import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import com.direwolf20.charginggadgets.capabilities.ChargerEnergyStorage;
import com.direwolf20.charginggadgets.capabilities.ChargerItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

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
    public ChargerItemHandler inventory = new ChargerItemHandler(this);

    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final ContainerData chargingStationData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ChargingStationTile.this.energyStorage.getAmountAsInt() / 32;
                case 1 -> ChargingStationTile.this.energyStorage.getCapacityAsInt() / 32;
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
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        assert level != null;
        return new ChargingStationContainer(this, this.chargingStationData, i, playerInventory, this.inventory);
    }

    public FuelValues getFuelValues() {
        return level.getServer().fuelValues();
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos blockPos, BlockState state, T t) {
        if (t instanceof ChargingStationTile entity) {
            entity.tryBurn();

            ItemStack stack = entity.getStackFromSlot(Slots.CHARGE.id);
            if (!stack.isEmpty())
                entity.chargeItem(stack);
        }
    }

    /**
     * Helper to get an ItemStack from a slot in the resource handler.
     */
    public ItemStack getStackFromSlot(int slot) {
        ItemResource resource = inventory.getResource(slot);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        return resource.toStack(inventory.getAmountAsInt(slot));
    }

    private void chargeItem(ItemStack stack) {
        EnergyHandler energy = stack.getCapability(Capabilities.Energy.ITEM, null);
        if (energy == null) return;

        // Check if the item can accept energy
        try (Transaction tx = Transaction.openRoot()) {
            if (energy.insert(1, tx) <= 0) return;
            // Don't commit — this was just a check
        }

        int toTransfer = Math.min(energyStorage.getAmountAsInt(), 2500);
        try (Transaction tx = Transaction.openRoot()) {
            int energyInserted = energy.insert(toTransfer, tx);
            tx.commit();
            energyStorage.consumeEnergy(energyInserted, false);
        }
    }

    private void tryBurn() {
        if (level == null)
            return;

        boolean canInsertEnergy = energyStorage.addEnergy(625, true) > 0;
        if (counter > 0 && canInsertEnergy) {
            burn();
        } else if (canInsertEnergy) {
            if (initBurn())
                burn();
        }
    }


    private void burn() {
        energyStorage.addEnergy(625, false);

        counter--;
        if (counter == 0) {
            maxBurn = 0;
            initBurn();
        }
    }

    private boolean initBurn() {
        ItemStack stack = getStackFromSlot(Slots.FUEL.id);

        int burnTime = stack.getBurnTime(RecipeType.SMELTING, getFuelValues());
        if (burnTime > 0) {
            ItemStack fuelStack = getStackFromSlot(Slots.FUEL.id);
            ItemStackTemplate remainderTemplate = fuelStack.getItem().getCraftingRemainder();
            if (remainderTemplate != null) {
                ItemStack remainder = remainderTemplate.create();
                // Set the remainder in the fuel slot
                try (Transaction tx = Transaction.openRoot()) {
                    inventory.extract(Slots.FUEL.id, inventory.getResource(Slots.FUEL.id), inventory.getAmountAsInt(Slots.FUEL.id), tx);
                    inventory.insert(Slots.FUEL.id, ItemResource.of(remainder), remainder.getCount(), tx);
                    tx.commit();
                }
            } else {
                // Shrink the fuel by 1
                try (Transaction tx = Transaction.openRoot()) {
                    inventory.extract(Slots.FUEL.id, inventory.getResource(Slots.FUEL.id), 1, tx);
                    tx.commit();
                }
            }

            setChanged();
            counter = (int) Math.floor(burnTime) / 50;
            maxBurn = counter;
            return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        input.child("inv").ifPresent(inventory::deserialize);
        energyStorage.deserialize(input);
        counter = input.getIntOr("counter", 0);
        maxBurn = input.getIntOr("maxburn", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        inventory.serialize(output.child("inv"));
        energyStorage.serialize(output);

        output.putInt("counter", counter);
        output.putInt("maxburn", maxBurn);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveCustomOnly(provider);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        this.loadAdditional(input);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        super.onDataPacket(net, input);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemResource r = inventory.getResource(i);
                if (!r.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), r.toStack(inventory.getAmountAsInt(i)));
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Charging Station Tile");
    }
}
