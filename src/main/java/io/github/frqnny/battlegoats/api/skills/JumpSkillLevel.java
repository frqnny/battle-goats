package io.github.frqnny.battlegoats.api.skills;

import net.minecraft.nbt.NbtCompound;

public class JumpSkillLevel extends SkillLevel {

    @Override
    public void updateSkill() {
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("JumpXP", this.xp);
        nbt.putInt("JumpLevel", this.level);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.xp = nbt.getInt("JumpXP");
        this.level = nbt.getInt("JumpLevel");
    }

    public float getJumpStrength() {
        return level * 0.02F;
    }
}
