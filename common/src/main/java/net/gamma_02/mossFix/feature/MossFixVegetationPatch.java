package net.gamma_02.mossFix.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.gamma_02.mossFix.mixin.WorldGenRegionAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import org.slf4j.Logger;

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
            //todo: Implement custom placement code here. This should (SHOULD) only happen when we're in game.
            LOGGER.info("Generated in server level!");
        }

        return super.place(featurePlaceContext);
    }
}
