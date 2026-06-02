package net.gamma_02.mossFix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.gamma_02.mossFix.MossFix;
import net.gamma_02.mossFix.feature.MossFixVegetationPatch;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Feature.class)
public class FeatureMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/Feature;register(Ljava/lang/String;Lnet/minecraft/world/level/levelgen/feature/Feature;)Lnet/minecraft/world/level/levelgen/feature/Feature;", ordinal = 19))
    private static <C extends FeatureConfiguration, F extends Feature<C>> F wrapRegistration(String string, F arg, Operation<F> original){
        MossFix.WHOLE_MOD_LOGGER.info("Redirecting VegetationPatch Registration! Registering in Minecraft namespace under id: {}", string);
        return original.call(string, new MossFixVegetationPatch(VegetationPatchConfiguration.CODEC));
    }
}
