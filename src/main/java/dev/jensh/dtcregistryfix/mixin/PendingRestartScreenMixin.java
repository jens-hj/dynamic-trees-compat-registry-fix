package dev.jensh.dtcregistryfix.mixin;

import net.Gabou.dynamictreescompat.client.screen.PendingRestartScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PendingRestartScreen.class, remap = false)
public abstract class PendingRestartScreenMixin extends Screen {
    @Shadow @Final private static Component MESSAGE;

    protected PendingRestartScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void dtcRegistryFix$drawTextAfterBackground(GuiGraphics graphics, int mouseX,
                                                      int mouseY, float partialTick, CallbackInfo callback) {
        // NeoForge's Screen.render already renders (and blurs) the background.
        // DTC calls it after drawing its message, blurring the message a second time.
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 52, 0xFFFFFF);
        graphics.drawWordWrap(font, MESSAGE, width / 2 - 160, height / 2 - 22, 320, 0xE0E0E0);
        callback.cancel();
    }
}
