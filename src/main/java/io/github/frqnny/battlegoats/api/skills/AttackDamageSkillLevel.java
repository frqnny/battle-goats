package io.github.frqnny.battlegoats.api.skills;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;

public class AttackDamageSkillLevel extends SkillLevel {
    protected final BattleGoatEntity goat;

    public AttackDamageSkillLevel(BattleGoatEntity goat) {
        this.goat = goat;
    }

    @Override
    public void updateSkill() {
        this.goat.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(level + 5);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("AttackDamageLevel", level);
        nbt.putInt("AttackDamageXP", xp);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.level = nbt.getInt("AttackDamageLevel");
        this.xp = nbt.getInt("AttackDamageXP");
        updateSkill();
    }
}
