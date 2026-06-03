package net.gamma_02.mossFix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MossBlock.class)
public class MossBlockMixin {

    //todo: config here as well -- when should moss blocks be bonemealable?
    @WrapOperation(method = "isValidBonemealTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    public boolean shouldBonemeal(BlockState instance,
                                  Operation<Boolean> original,
                                  LevelReader levelReader,
                                  BlockPos blockPos
    ){
        return !instance.isRedstoneConductor(levelReader, blockPos.above());
    }
}
