package dev.jensh.dtcregistryfix.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Queue;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerMixin {
    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;

    // NeoForge queues frozen-registry sync before mod configuration tasks. DTC must
    // first stage its authoritative pack and request a restart when IDs differ.
    // runConfiguration is added by NeoForge, so its name must not be remapped.
    @Inject(method = "runConfiguration", remap = false, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;startNextTask()V"))
    private void dtcRegistryFix$syncPackBeforeRegistries(CallbackInfo callback) {
        var ordered = new ArrayList<>(configurationTasks);
        for (int i = 1; i < ordered.size(); i++) {
            if (ordered.get(i).type().id().equals("dynamictreescompat:generated_pack_sync")) {
                var sync = ordered.remove(i);
                ordered.addFirst(sync);
                configurationTasks.clear();
                configurationTasks.addAll(ordered);
                LogUtils.getLogger().info("DTC Registry Fix: moved generated-pack synchronization before registry validation.");
                return;
            }
        }
    }
}
