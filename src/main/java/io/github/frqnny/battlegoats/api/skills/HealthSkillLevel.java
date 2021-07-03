package io.github.frqnny.battlegoats.api.skills;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;

public class HealthSkillLevel extends SkillLevel {
    protected final BattleGoatEntity goat;

    public HealthSkillLevel(BattleGoatEntity goat) {
        this.goat = goat;
    }

    @Override
    public void updateSkill() {
        goat.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue((level * 2) + 10);
        goat.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 50, Math.min(goat.world.getDifficulty().getId() - 1, 0)));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("HealthXP", this.xp);
        nbt.putInt("HealthLevel", this.level);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.xp = nbt.getInt("HealthXP");
        this.level = nbt.getInt("HealthLevel");
    }

}
