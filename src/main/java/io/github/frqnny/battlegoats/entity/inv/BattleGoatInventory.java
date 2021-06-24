package io.github.frqnny.battlegoats.entity.inv;

import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Predicate;


public class BattleGoatInventory implements Inventory {
    private final BattleGoatEntity battleGoat;
    private final int size;
    private final DefaultedList<ItemStack> stacks;

    public BattleGoatInventory(BattleGoatEntity battleGoat) {
        this.battleGoat = battleGoat;
        this.size = battleGoat.getInvSize();
        this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(stacks, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }

        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(stacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    @Override
    public void markDirty() {
        //TODO packet
        battleGoat.onInvChange();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (ItemStack stack : stacks) {
            if (predicate.test(stack)) {
                return true;
            }
        }

        return false;
    }

    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, stacks);
    }

    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, stacks);
    }


}
