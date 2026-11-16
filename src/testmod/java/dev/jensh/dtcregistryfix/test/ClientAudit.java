package dev.jensh.dtcregistryfix.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.Map;

public final class ClientAudit {
    private boolean connecting;
    private int joinedTicks;
    private long connectionStarted;
    private int screenTicks;
    private boolean screenshotRequested;

    public static void install() {
        ClientAudit audit = new ClientAudit();
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> audit.tick());
    }

    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (RegistryAudit.mode().equals("restart-screen")) {
            if (mc.screen instanceof TitleScreen) {
                mc.setScreen(new net.Gabou.dynamictreescompat.client.screen.PendingRestartScreen());
            }
            if (mc.screen instanceof net.Gabou.dynamictreescompat.client.screen.PendingRestartScreen
                    && ++screenTicks >= 40 && !screenshotRequested) {
                screenshotRequested = true;
                net.minecraft.client.Screenshot.grab(mc.gameDirectory, "restart-screen.png", mc.getMainRenderTarget(),
                        message -> mc.execute(mc::stop));
            }
            return;
        }
        if (!connecting && mc.screen instanceof TitleScreen) {
            connecting = true;
            connectionStarted = System.nanoTime();
            String address = System.getProperty("registry.audit.server");
            ConnectScreen.startConnecting(mc.screen, mc, ServerAddress.parseString(address),
                    new ServerData("Registry audit", address, ServerData.Type.OTHER), false, null);
        }
        if (connecting && (mc.screen instanceof DisconnectedScreen
                || System.nanoTime() - connectionStarted > 180_000_000_000L)) {
            RegistryAudit.write("connection-result.json", Map.of("passed", false,
                    "message", "Connection did not complete", "screen", mc.screen == null ? "none" : mc.screen.getClass().getName()));
            mc.stop();
        }
        if (mc.level != null && mc.player != null && ++joinedTicks >= 100) {
            RegistryAudit.write("connection-result.json", Map.of("passed", true, "message", "Joined isolated server and remained connected for 100 ticks"));
            mc.stop();
        }
    }
}
