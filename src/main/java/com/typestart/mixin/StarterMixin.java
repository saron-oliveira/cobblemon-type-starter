package com.typestart.mixin;

import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin que adiciona o tipo escolhido ao lado do nome do jogador na aba TAB.
 * Ex: "Steve [Fogo]"
 */
@Mixin(ServerPlayerEntity.class)
public class StarterMixin {

    /**
     * Sempre que o jogador aparece na lista do TAB, adiciona o tipo ao nome
     * visivel (display name).
     */
    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void onSpawn(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        updateTabName(player);
    }

    /**
     * Atualiza o nome no TAB apos o jogador fazer a escolha de tipo.
     * Chamado pelo TypeStartNetwork apos registrar a escolha.
     */
    public static void updateTabName(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        String type = TypeDataManager.getPlayerType(uuid);

        if (type != null) {
            String typeName    = StarterTypes.TYPE_DISPLAY_NAMES.getOrDefault(type, type);
            String colorCode   = getColorCode(type);
            // Define o display name: Nome [Tipo]
            player.setCustomName(Text.literal(
                player.getName().getString() + " " + colorCode + "[" + typeName + "]§r"
            ));
        } else {
            // Sem tipo ainda, remove customizacao
            player.setCustomName(null);
        }
    }

    /** Retorna o codigo de cor Minecraft para cada tipo */
    private static String getColorCode(String type) {
        return switch (type) {
            case "fire"     -> "§c"; // vermelho
            case "water"    -> "§9"; // azul
            case "grass"    -> "§a"; // verde
            case "electric" -> "§e"; // amarelo
            case "ice"      -> "§b"; // azul claro
            case "fighting" -> "§4"; // vermelho escuro
            case "poison"   -> "§5"; // roxo
            case "ground"   -> "§6"; // laranja
            case "flying"   -> "§3"; // ciano
            case "psychic"  -> "§d"; // rosa
            case "bug"      -> "§2"; // verde escuro
            case "rock"     -> "§8"; // cinza escuro
            case "ghost"    -> "§8"; // cinza
            case "dragon"   -> "§1"; // azul escuro
            case "dark"     -> "§8"; // cinza
            case "steel"    -> "§7"; // cinza claro
            case "fairy"    -> "§d"; // rosa
            case "normal"   -> "§f"; // branco
            default         -> "§f";
        };
    }
}
