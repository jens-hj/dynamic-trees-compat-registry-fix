package dev.jensh.dtcregistryfix.test.mixin;

import com.google.gson.JsonParser;
import net.Gabou.dynamictreescompat.registration.GeneratedTreeRuntimeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.nio.file.Files;
import java.nio.file.Path;

/** Force an existing snapshot to be eligible despite launcher temporary file paths. */
@Mixin(value = GeneratedTreeRuntimeCache.class, remap = false)
public abstract class CacheFixtureMixin {
    @Inject(method = "inputFingerprint", at = @At("RETURN"), cancellable = true, require = 1)
    private void registryAudit$cacheFixture(CallbackInfoReturnable<String> callback) {
        Path file = Path.of("dynamictreescompat/generation_cache.json");
        if (!Boolean.getBoolean("registry.audit.cacheFixture") || !Files.exists(file)) return;
        try (var reader = Files.newBufferedReader(file)) {
            callback.setReturnValue(JsonParser.parseReader(reader).getAsJsonObject().get("inputFingerprint").getAsString());
        } catch (Exception e) { throw new IllegalStateException("Invalid test cache fixture", e); }
    }
}
