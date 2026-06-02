package net.gamma_02.mossFix.mixin;

import net.gamma_02.mossFix.IPackRepository;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(value = PackRepository.class, priority = 0)
public class PackRepositoryMixin implements IPackRepository {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Override
    public void mossfix$addSource(RepositorySource source) {
        sources.add(source);
    }
}
