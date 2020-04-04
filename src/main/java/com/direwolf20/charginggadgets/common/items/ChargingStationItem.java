package com.direwolf20.charginggadgets.common.items;

import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import com.direwolf20.charginggadgets.common.utils.MagicHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.List;

public class ChargingStationItem extends BlockItem {

    public ChargingStationItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int power = stack.getOrCreateTag().getInt("energy");
        if( power == 0 )
            return;

        tooltip.add(new TranslationTextComponent("screen.charginggadgets.energy", MagicHelpers.withSuffix(power)).applyTextStyle(TextFormatting.GREEN));
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World worldIn, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof ChargingStationTile) {
            ChargingStationTile station = (ChargingStationTile) te;
            station.energyStorage.receiveEnergy(stack.getOrCreateTag().getInt("energy"), false);
        }

        return super.onBlockPlaced(pos, worldIn, player, stack, state);
    }
}
