package cn.autoforged.chinese_can_fly.mixin;

import cn.autoforged.chinese_can_fly.handler.FlightHandler;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerLanguageMixin {
    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void onUpdateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        FlightHandler.setClientLanguage(((ServerPlayer) (Object) this).getUUID(), packet.language());
    }
}
