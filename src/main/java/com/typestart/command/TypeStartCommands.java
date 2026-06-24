package com.typestart.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import com.typestart.network.TypeStartNetwork;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;

/**
 * Comandos admin do mod.
 *
 * /typestart reset all          -> reseta tudo (novo campeonato)
 * /typestart reset player <nome>-> reseta um jogador especifico
 * /typestart list               -> lista os tipos ja escolhidos e por quem
 * /typestart reopen <nome>      -> reabre a tela de escolha para um jogador
 */
public class TypeStartCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("typestart")
            .requires(src -> src.hasPermissionLevel(2)) // apenas ops

            // /typestart list
            .then(CommandManager.literal("list")
                .executes(ctx -> {
                    var source = ctx.getSource();
                    Map<String, String> taken = TypeDataManager.getAllTakenTypes();

                    if (taken.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("§eNenhum tipo foi escolhido ainda."), false);
                        return 1;
                    }

                    source.sendFeedback(() -> Text.literal("§e=== Tipos Escolhidos ==="), false);
                    taken.forEach((type, uuid) -> {
                        String typeName = StarterTypes.TYPE_DISPLAY_NAMES.getOrDefault(type, type);
                        String playerName = uuid;
                        var player = source.getServer().getPlayerManager().getPlayer(UUID.fromString(uuid));
                        if (player != null) playerName = player.getName().getString();

                        final String line = "§f" + typeName + " §7-> §a" + playerName;
                        source.sendFeedback(() -> Text.literal(line), false);
                    });
                    return 1;
                })
            )

            // /typestart reset all
            .then(CommandManager.literal("reset")
                .then(CommandManager.literal("all")
                    .executes(ctx -> {
                        TypeDataManager.resetAll();
                        TypeDataManager.save(ctx.getSource().getServer());
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("§aTodos os tipos foram resetados. Novo campeonato!"), true
                        );
                        return 1;
                    })
                )

                // /typestart reset player <nome>
                .then(CommandManager.literal("player")
                    .then(CommandManager.argument("jogador", StringArgumentType.word())
                        .executes(ctx -> {
                            String nome = StringArgumentType.getString(ctx, "jogador");
                            var server  = ctx.getSource().getServer();
                            ServerPlayerEntity target = server.getPlayerManager().getPlayer(nome);

                            if (target == null) {
                                ctx.getSource().sendFeedback(
                                    () -> Text.literal("§cJogador nao encontrado: " + nome), false
                                );
                                return 0;
                            }

                            TypeDataManager.resetPlayer(target.getUuidAsString());
                            TypeDataManager.save(server);
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("§aEscolha de " + nome + " foi resetada."), true
                            );
                            return 1;
                        })
                    )
                )
            )

            // /typestart reopen <nome>  -> reabre a tela para o jogador (sem resetar)
            .then(CommandManager.literal("reopen")
                .then(CommandManager.argument("jogador", StringArgumentType.word())
                    .executes(ctx -> {
                        String nome   = StringArgumentType.getString(ctx, "jogador");
                        var server    = ctx.getSource().getServer();
                        ServerPlayerEntity target = server.getPlayerManager().getPlayer(nome);

                        if (target == null) {
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("§cJogador nao encontrado: " + nome), false
                            );
                            return 0;
                        }

                        // Reseta e reabre
                        TypeDataManager.resetPlayer(target.getUuidAsString());
                        TypeDataManager.save(server);
                        ServerPlayNetworking.send(target, new TypeStartNetwork.OpenTypeScreenPayload());

                        ctx.getSource().sendFeedback(
                            () -> Text.literal("§aTela de escolha reaberta para " + nome + "."), true
                        );
                        return 1;
                    })
                )
            )
        );
    }
}
