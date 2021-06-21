package io.github.frqnny.battlegoats.api.skills;

import net.minecraft.nbt.NbtCompound;

public abstract class SkillLevel {

    public abstract void updateSkill();

    public abstract void writeNbt(NbtCompound nbt);

    public abstract void readNbt(NbtCompound nbt);


}
