package games.enchanted.eg_more_multiplayer_players.common.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import games.enchanted.eg_more_multiplayer_players.common.duck.IntegratedServerAdditions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.MultiplayerOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(MultiplayerOptionsScreen.class)
public abstract class MultiplayerOptionsScreenMixin extends Screen {
    @Shadow
    protected abstract void updateApplyChangesActiveState();

    @Unique
    private static final Component SERVER_SETTINGS_HEADER = Component.literal("Server Settings").withStyle(Style.EMPTY.withBold(true).withUnderlined(true));

    @Unique
    private @Nullable EditBox maxPlayersBox;
    @Unique
    private int maxPlayers = 8;
    @Unique
    private boolean maxPlayersValid = true;
    @Unique
    private static final Component MAX_PLAYERS_TEXT = Component.literal("Maximum Players");

    protected MultiplayerOptionsScreenMixin(Component title) {
        super(title);
    }

    @Definition(id = "labeledElement", method = "Lnet/minecraft/client/gui/layouts/CommonLayouts;labeledElement(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/layouts/LayoutElement;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/layouts/Layout;")
    @Definition(id = "portEdit", field = "Lnet/minecraft/client/gui/screens/MultiplayerOptionsScreen;portEdit:Lnet/minecraft/client/gui/components/EditBox;")
    @Definition(id = "PORT_INFO_TEXT", field = "Lnet/minecraft/client/gui/screens/MultiplayerOptionsScreen;PORT_INFO_TEXT:Lnet/minecraft/network/chat/Component;")
    @Expression("labeledElement(?, this.portEdit, PORT_INFO_TEXT)")
    @Inject(
        slice = @Slice(
            from = @At(value = "MIXINEXTRAS:EXPRESSION")
        ),
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        method = "init"
    )
    private void eg_more_multiplayer_players$injectMaxPlayersBox(CallbackInfo ci, @Local(name = "content") LinearLayout content) {
        content.addChild(new StringWidget(SERVER_SETTINGS_HEADER, this.font));

        this.maxPlayersBox = new EditBox(this.font, MAX_PLAYERS_TEXT);
        this.maxPlayersBox.setResponder((value) -> {
            Component errorMessage = this.eg_more_multiplayer_players$parseMaxPlayers(value);
            this.maxPlayersBox.setHint(Component.literal("" + this.maxPlayers));
            if (errorMessage == null) {
                this.maxPlayersValid = true;
                this.maxPlayersBox.setTextColor(-2039584);
                this.maxPlayersBox.setTooltip(null);
            } else {
                this.maxPlayersValid = false;
                this.maxPlayersBox.setTextColor(-2142128);
                this.maxPlayersBox.setTooltip(Tooltip.create(errorMessage));
            }

            this.updateApplyChangesActiveState();
        });
        this.maxPlayersBox.setHint(Component.literal("" + this.maxPlayers));
        content.addChild(CommonLayouts.labeledElement(this.font, this.maxPlayersBox, MAX_PLAYERS_TEXT));
    }

    @Unique
    private @Nullable Component eg_more_multiplayer_players$parseMaxPlayers(String value) {
        if(value.isBlank()) {
            this.maxPlayers = 8;
            return null;
        }
        try {
            int parsed = Integer.parseInt(value);
            if(parsed < 0 || parsed > 150) {
                return Component.literal("Must be between 1 and 150");
            }
            this.maxPlayers = parsed;
        } catch (NumberFormatException e) {
            return Component.literal("Not a number");
        }
        return null;
    }


    @WrapOperation(
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/MultiplayerOptionsScreen;hasSettingsChanges()Z"),
        method = "updateApplyChangesActiveState"
    )
    private boolean eg_more_multiplayer_players$wrapMaxPlayersCheck(MultiplayerOptionsScreen instance, Operation<Boolean> original) {
        return original.call(instance) && this.maxPlayersValid;
    }


    @WrapOperation(
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;publishServer(Lnet/minecraft/world/level/GameType;ZI)Z"),
        method = "publish"
    )
    private boolean eg_more_multiplayer_players$wrapPublish(IntegratedServer instance, GameType gameMode, boolean allowCommands, int port, Operation<Boolean> original) {
        boolean orig = original.call(instance, gameMode, allowCommands, port);
        if(instance instanceof IntegratedServerAdditions additions) {
            additions.eg_more_multiplayer_players$setMaxPlayers(this.maxPlayers);
        }
        return orig;
    }
}
