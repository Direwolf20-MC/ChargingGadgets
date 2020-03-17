package com.direwolf20.charginggadgets.common.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class ChargingStationItem extends BlockItem {

    public ChargingStationItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

// todo: implement energy persistence over block breaks :D
//    @Override
//    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
//        super.addInformation(stack, worldIn, tooltip, flagIn);
//        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
//            tooltip.add(new StringTextComponent("Energy: " + energy.getEnergyStored()));
//        });
//    }

// todo: implement energy persistence over block breaks :D
//    @Override
//    protected boolean onBlockPlaced(BlockPos pos, World worldIn, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
//        TileEntity te = worldIn.getTileEntity(pos);
//        if (te instanceof ChargingStationTile) {
//            ChargingStationTile station = (ChargingStationTile) te;
//            station.getEnergy().ifPresent(e -> stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(a -> e.receiveEnergy(a.getEnergyStored(), false)));
//        }
//
//        return super.onBlockPlaced(pos, worldIn, player, stack, state);
//    }
//
//    @Nullable
//    @Override
//    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
//        return new EnergyCapabilityProvider(stack, 1000000);
//    }
//
//    static class EnergyCapabilityProvider implements ICapabilityProvider {
//        private final ItemEnergyStorage energyItem;
//        private LazyOptional<ItemEnergyStorage> energyCapability;
//
//        public EnergyCapabilityProvider(ItemStack stack, int energyCapacity) {
//            this.energyItem = new ItemEnergyStorage(stack, energyCapacity);
//            this.energyCapability = LazyOptional.of(() -> energyItem);
//        }
//
//        @Nonnull
//        @Override
//        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//            return cap == CapabilityEnergy.ENERGY ? energyCapability.cast() : LazyOptional.empty();
//        }
//    }
}
