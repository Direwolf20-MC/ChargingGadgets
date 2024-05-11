package com.direwolf20.charginggadgets.blocks.chargingstation;

import com.direwolf20.charginggadgets.CGDataComponents;
import com.direwolf20.charginggadgets.blocks.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;
import java.util.List;


public class ChargingStationBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ChargingStationBlock() {
        super(Properties.of().strength(2f));

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : ChargingStationTile::ticker;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity te = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        List<ItemStack> drops = super.getDrops(state, builder);
        if (te instanceof ChargingStationTile) {
            ChargingStationTile tileEntity = (ChargingStationTile) te;
            drops.stream()
                    .filter(e -> e.getItem() instanceof ChargingStationItem)
                    .findFirst()
                    .ifPresent(e -> e.set(CGDataComponents.ENERGY, tileEntity.energyStorage.getEnergyStored()));
        }

        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity != null) {
                //LazyOptional<IItemHandler> cap = tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                var cap = worldIn.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, tileEntity, null);
                if (cap != null) {
                    for (int i = 0; i < cap.getSlots(); i++)
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), cap.getStackInSlot(i));
                }
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult hit) {
        // Only execute on the server
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity te = level.getBlockEntity(blockPos);
        if (!(te instanceof ChargingStationTile))
            return InteractionResult.FAIL;

        player.openMenu((MenuProvider) te, blockPos);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockRegistry.CHARGING_STATION_TILE.get().create(pos, state);
    }
}
