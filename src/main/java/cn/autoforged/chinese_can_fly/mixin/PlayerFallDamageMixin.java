package cn.autoforged.chinese_can_fly.mixin;

import cn.autoforged.chinese_can_fly.config.ModConfig;
import cn.autoforged.chinese_can_fly.handler.FlightHandler;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerFallDamageMixin {

    @Redirect(
        method = "causeFallDamage",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z")
    )
    private boolean allowFallDamageWhenNotFlying(Abilities abilities) {
        if (!abilities.mayfly) return false;
        if (abilities.instabuild) return true;
        Player player = (Player) (Object) this;
        if (!FlightHandler.isFlightPermitted(player)) return true;
        if (player.getAbilities().flying) return true;
        if (ModConfig.get().preventFallDamageWhenNotFlying) return true;
        return false;
    }
}
