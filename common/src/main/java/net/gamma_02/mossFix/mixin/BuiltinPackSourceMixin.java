package net.gamma_02.mossFix.mixin;

import net.gamma_02.mossFix.MossFix;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(BuiltInPackSource.class)
public class BuiltinPackSourceMixin {


    @Shadow
    @Final
    private PackType packType;

    @Inject(method = "loadPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/BuiltInPackSource;listBundledPacks(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE))
    private void thing(Consumer<Pack> consumer, CallbackInfo ci){

        if(packType == PackType.SERVER_DATA) {
            consumer.accept(MossFix.SPREAD_COBBLES.toPack());
            consumer.accept(MossFix.SPREAD_SANDS.toPack());
        }
    }
}
