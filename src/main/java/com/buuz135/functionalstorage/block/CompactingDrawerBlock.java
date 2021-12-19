package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompactingDrawerBlock extends RotatableBlock<CompactingDrawerTile> {

    public static Multimap<Direction, VoxelShape> CACHED_SHAPES = MultimapBuilder.hashKeys().arrayListValues().build();

    static {
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(1/16D, 1/16D, 0, 8/16D, 8/16D, 1/16D));
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(8/16D, 1/16D, 0, 15/16D, 8/16D, 1/16D));
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(1/16D, 8/16D, 0, 15/16D, 15/16D, 1/16D));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(8/16D, 1/16D, 15/16D, 15/16D, 8/16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(1/16D, 1/16D, 15/16D, 8/16D, 8/16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(1/16D, 8/16D, 15/16D, 15/16D, 15/16D, 1));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 1/16D, 1/16D, 1, 8/16D, 7/16D));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 1/16D, 8/16D, 1, 8/16D, 15/16D));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 8/16D, 1/16D, 1, 15/16D, 15/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 1/16D, 8/16D, 1/16D, 8/16D, 15/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 1/16D, 1/16D, 1/16D, 8/16D, 7/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 8/16D, 1/16D, 1/16D, 15/16D, 15/16D));
    }


    public CompactingDrawerBlock(String name) {
        super(name, Properties.copy(Blocks.OAK_PLANKS), CompactingDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH));
    }

    @Override
    public void addAlternatives(DeferredRegistryHelper registry) {
        super.addAlternatives(registry);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<CompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new CompactingDrawerTile(this, blockPos, state);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos){
        List<VoxelShape> boxes = new ArrayList<>();
        CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add); //TODO
        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return Shapes.box(0, 0, 0, 1,1,1);
    }

    @Override
    public boolean hasCustomBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasIndividualRenderVoxelShape() {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        return TileUtil.getTileEntity(worldIn, pos, CompactingDrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(InteractionResult.PASS);
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
       TileUtil.getTileEntity(worldIn, pos, CompactingDrawerTile.class).ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, Level worldIn, BlockPos pos, Player player) {
        HitResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockHitResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockHitResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(Shapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>(CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL))); //TODO
                for (int i = 0; i < shapes.size(); i++) {
                    if (Shapes.joinIsNotEmpty(shapes.get(i), hit, BooleanOp.AND)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public LootTable.Builder getLootTable(@Nonnull BasicBlockLootTables blockLootTables) {
        CopyNbtFunction.Builder nbtBuilder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
        nbtBuilder.copy("handler",  "BlockEntityTag.handler");
        return blockLootTables.droppingSelfWithNbt(this, nbtBuilder);
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

}
