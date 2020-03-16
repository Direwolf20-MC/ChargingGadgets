package com.direwolf20.charginggadgets.common.blocks;

import com.direwolf20.charginggadgets.common.capabilities.ItemHandlerWrapper;
import com.direwolf20.charginggadgets.common.items.ChargingStationItem;
import com.direwolf20.charginggadgets.common.tiles.ChargingStationTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.List;

public class ChargingStationBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ChargingStationBlock() {
        super(Properties.create(Material.ROCK).hardnessAndResistance(2f).harvestTool(ToolType.PICKAXE));

        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);

        List<ItemStack> drops = super.getDrops(state, builder);
        if (te instanceof ChargingStationTile) {
            ChargingStationTile tileEntity = (ChargingStationTile) te;
            drops.stream()
                    .filter(e -> e.getItem() instanceof ChargingStationItem)
                    .findFirst()
                    .ifPresent(e -> this.setupItem(tileEntity, e));
        }

        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if( newState.getBlock() != this ) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                LazyOptional<IItemHandler> cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                cap.ifPresent(handler -> {
                    if (handler instanceof IItemHandlerModifiable)
                        InventoryHelper.dropInventoryItems(worldIn, pos, new ItemHandlerWrapper((IItemHandlerModifiable) handler));
                });
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    public void setupItem(ChargingStationTile tile, ItemStack item) {
        tile.getEnergy().ifPresent(e -> item.getCapability(CapabilityEnergy.ENERGY).ifPresent(a -> a.receiveEnergy(e.getMaxEnergyStored(), false)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        TileEntity te = worldIn.getTileEntity(pos);
        if (! (te instanceof ChargingStationTile))
            return ActionResultType.FAIL;

        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModBlocks.CHARGING_STATION_TILE.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
