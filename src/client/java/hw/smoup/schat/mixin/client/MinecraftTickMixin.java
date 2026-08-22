package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatTabs;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void schat$tick(CallbackInfo ci) {
        ChatTabs.onClientTick();
    }
}
