package io.github.frqnny.battlegoats.mixin;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkItem.class)
public class MixinFireworkItem {

    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> i) {

        if (user.hasVehicle() && user.getVehicle() instanceof BattleGoatEntity goat && goat.isFallFlying()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (!world.isClient) {
                FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, itemStack, goat);
                world.spawnEntity(fireworkRocketEntity);
                if (!user.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                user.incrementStat(Stats.USED.getOrCreateStat(((FireworkItem) (Object) this)));
            }
        }
    }
}
