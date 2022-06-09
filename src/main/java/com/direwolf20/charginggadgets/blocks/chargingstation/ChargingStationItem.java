package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.Config;
import com.direwolf20.charginggadgets.utils.MagicHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ChargingStationItem extends BlockItem {

    public ChargingStationItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        int power = stack.getOrCreateTag().getInt("energy");
        if (power == 0)
            return;

        tooltip.add(Component.translatable("screen.charginggadgets.energy", MagicHelpers.withSuffix(power), MagicHelpers.withSuffix(Config.GENERAL.chargerMaxPower.get())).withStyle(ChatFormatting.GREEN));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof ChargingStationTile) {
            ChargingStationTile station = (ChargingStationTile) te;
            station.energyStorage.receiveEnergy(stack.getOrCreateTag().getInt("energy"), false);
        }

        return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
    }
}
