package net.gamma_02.mossFix.feature;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Idea for detecting bonemeal configured feature:
 * depth: ConstantInt.of(1)
 * xZRadius: UniformInt.of(1, 2)
 *
 *  OR!!
 *
 *  chunk generator phase?
 */
public class MossFixVegetationPatch extends VegetationPatchFeature {
    private static Logger LOGGER = LogUtils.getLogger();

    public MossFixVegetationPatch(Codec<VegetationPatchConfiguration> codec) {
        super(codec);
    }


    @Override
    public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext) {

        WorldGenLevel level = featurePlaceContext.level();


        if(level instanceof WorldGenRegion){
            return super.place(featurePlaceContext);
        }
        else if (level instanceof ServerLevel)
        {
            //todo: Implement custom placement code here. This should (SHOULD) only happen when we're bonemealing moss!
            LOGGER.info("Generated in server level!");
            return bonemealFixPlace(featurePlaceContext);
        }

        return super.place(featurePlaceContext);
    }

    public boolean bonemealFixPlace(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext){
        WorldGenLevel level = featurePlaceContext.level();
        VegetationPatchConfiguration config = featurePlaceContext.config();
        RandomSource rand = featurePlaceContext.random();
        BlockPos origin = featurePlaceContext.origin();
        Predicate<BlockState> isReplacable = blockState -> blockState.is(config.replaceable);


        int xSample = config.xzRadius.sample(rand) + 1;
        int zSample = config.xzRadius.sample(rand) + 1;

        Set<Pair<BlockPos, Boolean>> mossPosAirAboveSet = this.bonemealPlaceGroundPatch(level, config, rand, origin, isReplacable, xSample, zSample);
        this.bonemealDistributeVegetation(featurePlaceContext, level, config, rand, mossPosAirAboveSet);
        return !mossPosAirAboveSet.isEmpty();
    }

    public Set<Pair<BlockPos, Boolean>> bonemealPlaceGroundPatch(
            WorldGenLevel level,
            VegetationPatchConfiguration config,
            RandomSource rand,
            BlockPos origin,
            Predicate<BlockState> isReplacable,
            int xSample,
            int zSample
    ) {
        BlockPos.MutableBlockPos vegetationPos = origin.mutable();
        BlockPos.MutableBlockPos mossPos = vegetationPos.mutable();
        Direction surfaceDown = config.surface.getDirection();
        Direction surfaceUp = surfaceDown.getOpposite();
        Set<Pair<BlockPos, Boolean>> mossPosAirAboveSet = new HashSet<>();

        for (int x = -xSample; x <= xSample; x++) {
            boolean onXEdge = x == -xSample || x == xSample;

            for (int z = -zSample; z <= zSample; z++) {
                boolean onZEdge = z == -zSample || z == zSample;
                boolean onAnyEdge = onXEdge || onZEdge;
                boolean onCorner = onXEdge && onZEdge;

                boolean placeExtraEdgeColumn =
                        config.extraEdgeColumnChance != 0.0F &&
                        !(rand.nextFloat() > config.extraEdgeColumnChance);

                //if we are not on a corner and we are either not on an edge or we should place an extra edge column
                if (!onCorner && (!onAnyEdge || placeExtraEdgeColumn))
                {
                    vegetationPos.setWithOffset(origin, x, 0, z);

                    //move vegetationPos so that it's the block above a moss position
                    boolean hasEmptyBlockAbove = moveToNonSolidAboveMoss(level, config, vegetationPos, surfaceDown, surfaceUp);

                    //we can place moss below any non-full block, so we should continue if it is a "full block", or non-transparent block.
                    //todo: config
                    if(level.getBlockState(vegetationPos).isRedstoneConductor(level, vegetationPos))
                        continue;

                    //set moss position
                    mossPos.setWithOffset(vegetationPos, surfaceDown);
                    BlockState blockState = level.getBlockState(mossPos);

                    //original moss place check: if the block above is empty and the face of the block below is "sturdy"
                    // level.isEmptyBlock(vegetationPos) && blockState.isFaceSturdy(level, mossPos, surfaceUp)
                    
                    //since we've already checked if we can place moss below,
                    // we just need to check if the moss block's face is sturdy ( or another config predicate )
                    //todo: config: we should make it able to replace blocks like. stairs, things with mostly full hitboxes.
                    // maybe even anything in the tag! who knows
                    // ~~air~~
                    if (blockState.isFaceSturdy(level, mossPos, surfaceUp)) {
                        boolean addExtraBottomBlock = config.extraBottomBlockChance > 0.0F &&
                                rand.nextFloat() < config.extraBottomBlockChance;

                        int maxDepth = config.depth.sample(rand)
                                + ( addExtraBottomBlock ? 1 : 0);

                        BlockPos immutableMossPos = mossPos.immutable();

                        if (
                            this.bonemealPlaceGround(
                                level,
                                config,
                                isReplacable,
                                rand,
                                mossPos,
                                maxDepth
                            )
                        ) {
                            //todo: config -- should we make this able to replace things like vines, leaves, etc. ?
                            mossPosAirAboveSet.add(Pair.of(immutableMossPos, hasEmptyBlockAbove));
                        }
                    }
                }
            }
        }

        return mossPosAirAboveSet;
    }

    private static @NotNull Predicate<BlockState> stateIsFullBlock(WorldGenLevel level, BlockPos pos) {
        return (state) -> state.isRedstoneConductor(level, pos);
    }

    private static @NotNull Predicate<BlockState> stateIsNotFullBlock(WorldGenLevel level, BlockPos pos) {
        return (state) -> !state.isRedstoneConductor(level, pos);
    }

    /**
     * This will shift the given vegetationPos first down, then up to find the bottom block to place the feature's ground.
     *
     * @param level Level moss is being bonemealed in
     * @param config Feature config
     * @param vegetationPos Pos to move while searching, one block above the moss block with whatever offset from the loop
     * @param surfaceDown Down for config.surface == FLOOR, up for config.surface == CEILING
     * @param surfaceUp Opposite of surfaceDown
     * @return if there is an empty block at vegetationPos after moving it
     */
    private static boolean moveToNonSolidAboveMoss(WorldGenLevel level,
                                           VegetationPatchConfiguration config,
                                           BlockPos.MutableBlockPos vegetationPos,
                                           Direction surfaceDown,
                                           Direction surfaceUp) {
        for (int y = 0;
             level.isStateAtPosition(vegetationPos, stateIsNotFullBlock(level, vegetationPos)) && y < config.verticalRange;
             y++
        ) {
            vegetationPos.move(surfaceDown);
        }

        for (int y = 0;
             level.isStateAtPosition(vegetationPos, stateIsFullBlock(level, vegetationPos)) && y < config.verticalRange;
             y++
        ) {
            vegetationPos.move(surfaceUp);
        }

        return level.isEmptyBlock(vegetationPos);
    }

    protected void bonemealDistributeVegetation(
            FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext,
            WorldGenLevel worldGenLevel,
            VegetationPatchConfiguration vegetationPatchConfiguration,
            RandomSource randomSource,
            Set<Pair<BlockPos, Boolean>> mossPosAirAboveSet
    ) {
        for (Pair<BlockPos, Boolean> pair : mossPosAirAboveSet) {
            BlockPos pos = pair.getFirst();
            
            //if we randomly can place vegetation and the blockPos
            if (
                    vegetationPatchConfiguration.vegetationChance > 0.0F && 
                    randomSource.nextFloat() < vegetationPatchConfiguration.vegetationChance &&
                    pair.getSecond()
            ) {
                //place vegetation one above each block pos
                this.placeVegetation(
                    worldGenLevel,
                    vegetationPatchConfiguration,
                    featurePlaceContext.chunkGenerator(),
                    randomSource,
                    pos
                );
            }
        }
    }

    /**
     *
     * @param level Level moss is bonemealed in
     * @param config Feature config
     * @param isStateReplacable Predicate determining if a state can be replaced with moss
     * @param rand random source for placement
     * @param mossPos position to place from
     * @param maxDepth Depth for placement searching
     * @return true iff moss was placed at mossPos
     */
    protected boolean bonemealPlaceGround(
            WorldGenLevel level,
            VegetationPatchConfiguration config,
            Predicate<BlockState> isStateReplacable,
            RandomSource rand,
            BlockPos.MutableBlockPos mossPos,
            int maxDepth
    ) {
        for (int j = 0; j < maxDepth; j++) {

            BlockState mossState = config.groundState.getState(rand, mossPos);
            BlockState existingState = level.getBlockState(mossPos);

            if (!mossState.is(existingState.getBlock())) {

                if (!isStateReplacable.test(existingState)) {
                    return j != 0;
                }

                level.setBlock(mossPos, mossState, 2);

                mossPos.move(config.surface.getDirection());
            }
        }

        return true;
    }
}
