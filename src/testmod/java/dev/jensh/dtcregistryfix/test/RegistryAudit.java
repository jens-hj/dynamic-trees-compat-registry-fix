package dev.jensh.dtcregistryfix.test;

import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod("registry_audit")
public final class RegistryAudit {
    public RegistryAudit(IEventBus bus) {
        bus.addListener(this::complete);
        if (FMLEnvironment.dist == Dist.CLIENT && !mode().equals("snapshot")) ClientAudit.install();
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ServerStartedEvent event) -> {
                write("server-start-result.json", Map.of("passed", true, "message", "World loaded and server started"));
                if (mode().equals("worldcheck")) event.getServer().halt(false);
            });
        }
    }

    static String mode() { return System.getProperty("registry.audit.mode", "snapshot"); }

    private void complete(FMLLoadCompleteEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("blocks", BuiltInRegistries.BLOCK.keySet().stream().map(Object::toString).sorted().toList());
        data.put("items", BuiltInRegistries.ITEM.keySet().stream().map(Object::toString).sorted().toList());
        write("registry-snapshot.json", data);
        if (mode().equals("snapshot")) System.exit(0);
    }

    static void write(String filename, Object data) {
        try {
            Files.writeString(Path.of(filename), new GsonBuilder().setPrettyPrinting().create().toJson(data));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
