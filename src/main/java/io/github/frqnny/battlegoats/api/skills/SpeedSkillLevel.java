package io.github.frqnny.battlegoats.api.skills;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;

public class SpeedSkillLevel extends SkillLevel {
    protected final BattleGoatEntity goat;

    public SpeedSkillLevel(BattleGoatEntity goat) {
        this.goat = goat;
    }

    @Override
    public void updateSkill() {
        goat.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.20000000298023224D + (level * 0.02));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("SpeedXP", this.xp);
        nbt.putInt("SpeedLevel", this.level);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.xp = nbt.getInt("SpeedXP");
        this.level = nbt.getInt("SpeedLevel");

    }
}
