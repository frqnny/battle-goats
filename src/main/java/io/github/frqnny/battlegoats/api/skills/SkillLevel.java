package io.github.frqnny.battlegoats.api.skills;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public abstract class SkillLevel {
    public int level = 0;
    public int xp;

    public static int xpNeededForLevel(int x) {
        return (int) (((x * x) * 5 / 1.8 + 30 - (x * x)) * 12);
    }

    public abstract void updateSkill();

    public abstract void writeNbt(NbtCompound nbt);

    public abstract void readNbt(NbtCompound nbt);

    public void fromBuf(PacketByteBuf buf) {
        level = buf.readInt();
        xp = buf.readInt();
    }

    public void toBuf(PacketByteBuf buf) {
        buf.writeInt(level);
        buf.writeInt(xp);
    }

    public void addXp(int amount) {
        int maxXp = xpNeededForLevel(level);
        if (xp + amount >= maxXp) {
            level++;
            xp = xp + amount - maxXp;
            updateSkill();
        } else {
            xp += amount;
        }
    }
}
