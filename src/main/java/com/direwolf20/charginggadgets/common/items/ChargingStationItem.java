package com.direwolf20.charginggadgets.common.items;

import com.direwolf20.charginggadgets.common.capabilities.ItemEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

public class ChargingStationItem extends BlockItem {

    public ChargingStationItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            tooltip.add(new StringTextComponent("Energry: " + energy.getEnergyStored()));
        });
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new EnergyCapabilityProvider(stack, 1000000);
    }

    static class EnergyCapabilityProvider implements ICapabilityProvider {
        private final ItemEnergyStorage energyItem;
        private LazyOptional<ItemEnergyStorage> energyCapability;

        public EnergyCapabilityProvider(ItemStack stack, int energyCapacity) {
            this.energyItem = new ItemEnergyStorage(stack, energyCapacity);
            this.energyCapability = LazyOptional.of(() -> energyItem);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == CapabilityEnergy.ENERGY ? energyCapability.cast() : LazyOptional.empty();
        }
    }
}
