package dev.jensh.dtcregistryfix.mixin;

import com.mojang.logging.LogUtils;
import net.Gabou.dynamictreescompat.detection.MoonlightAutoFinderRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

/** Cache snapshots contain broader worldgen candidates than the startup resource scan. */
@Mixin(value = MoonlightAutoFinderRegistrar.class, remap = false)
public abstract class MoonlightAutoFinderRegistrarMixin {
    @Shadow public abstract void register();

    @Inject(method = "register(Ljava/util/Collection;)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void dtcRegistryFix$scanResources(Collection<?> ignoredCandidates, CallbackInfo callback) {
        LogUtils.getLogger().info("DTC Registry Fix: scanning wood resources instead of registering cached discovery candidates; generation cache remains enabled.");
        register();
        callback.cancel();
    }
}
