package games.enchanted.eg_more_multiplayer_players.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import games.enchanted.eg_more_multiplayer_players.common.duck.IntegratedServerAdditions;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IntegratedServer.class, priority = 1050)
public abstract class IntegratedServerMixin implements IntegratedServerAdditions {
    @Unique
    private int eg_more_multiplayer_players$maxPlayers = IntegratedServer.MAX_PLAYERS;

    @ModifyReturnValue(
        at = @At("TAIL"),
        method = "getMaxPlayers"
    )
    private int eg_more_multiplayer_players$modifyMaxPlayers(int original) {
        return this.eg_more_multiplayer_players$maxPlayers;
    }

    @Override
    public void eg_more_multiplayer_players$setMaxPlayers(int players) {
        this.eg_more_multiplayer_players$maxPlayers = players;
    }
}
