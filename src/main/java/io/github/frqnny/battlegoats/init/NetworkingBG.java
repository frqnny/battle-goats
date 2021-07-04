package io.github.frqnny.battlegoats.init;

import io.github.frqnny.battlegoats.BattleGoats;
import io.github.frqnny.battlegoats.entity.BattleGoatEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class NetworkingBG {
    public static final Identifier SYNC_INV = BattleGoats.id("sync_inv");

    public static void clientInit() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_INV, ((client, handler, buf, responseSender) -> {

        }));
    }
}
