package com.typestart.util;

import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Mostra o tipo escolhido ao lado do nome do jogador (lista TAB, nome acima da
 * cabeca e chat). Ex: "Steve [Fogo]".
 *
 * Usa a API publica de TIMES (scoreboard) do Minecraft em vez de um mixin.
 * Assim nao ha risco de crash no carregamento e a tag aparece de verdade na
 * lista TAB (um sufixo de time eh sincronizado automaticamente para os clientes).
 */
public final class TabName {

    private TabName() {}

    // Prefixo dos times criados por este mod (um time por tipo).
    private static final String TEAM_PREFIX = "ts_";

    /** Coloca o jogador no time do seu tipo, criando/atualizando o time se preciso. */
    public static void apply(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        String type = TypeDataManager.getPlayerType(player.getUuidAsString());
        if (type == null) {
            clear(player);
            return;
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        String teamName = TEAM_PREFIX + type;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
        }

        String typeName = StarterTypes.TYPE_DISPLAY_NAMES.getOrDefault(type, type);
        team.setSuffix(Text.literal(" [" + typeName + "]").formatted(colorOf(type)));

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
    }

    /** Remove o jogador do time do mod (usado nos comandos de reset). */
    public static void clear(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerScoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
        if (team != null && team.getName().startsWith(TEAM_PREFIX)) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), team);
        }
    }

    /** Cor (formatacao Minecraft) de cada tipo, usada no sufixo da TAB. */
    private static Formatting colorOf(String type) {
        return switch (type) {
            case "fire"     -> Formatting.RED;
            case "water"    -> Formatting.BLUE;
            case "grass"    -> Formatting.GREEN;
            case "electric" -> Formatting.YELLOW;
            case "ice"      -> Formatting.AQUA;
            case "fighting" -> Formatting.DARK_RED;
            case "poison"   -> Formatting.DARK_PURPLE;
            case "ground"   -> Formatting.GOLD;
            case "flying"   -> Formatting.DARK_AQUA;
            case "psychic"  -> Formatting.LIGHT_PURPLE;
            case "bug"      -> Formatting.DARK_GREEN;
            case "rock"     -> Formatting.DARK_GRAY;
            case "ghost"    -> Formatting.DARK_GRAY;
            case "dragon"   -> Formatting.DARK_BLUE;
            case "dark"     -> Formatting.DARK_GRAY;
            case "steel"    -> Formatting.GRAY;
            case "fairy"    -> Formatting.LIGHT_PURPLE;
            case "normal"   -> Formatting.WHITE;
            default         -> Formatting.WHITE;
        };
    }
}
