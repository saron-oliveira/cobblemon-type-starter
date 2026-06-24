package com.typestart.network;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.typestart.TypeStartMod;
import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import com.typestart.screen.TypeSelectScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class TypeStartNetwork {

    // Pacote: servidor manda para cliente abrir a tela de escolha de tipo
    public static final Identifier OPEN_TYPE_SCREEN = new Identifier(TypeStartMod.MOD_ID, "open_type_screen");

    // Pacote: cliente manda para servidor informando o tipo escolhido
    public static final Identifier TYPE_CHOSEN = new Identifier(TypeStartMod.MOD_ID, "type_chosen");

    // Pacote: cliente manda para servidor informando o Pokemon escolhido
    public static final Identifier POKEMON_CHOSEN = new Identifier(TypeStartMod.MOD_ID, "pokemon_chosen");

    // Pacote: servidor confirma escolha e fecha tela
    public static final Identifier CHOICE_RESULT = new Identifier(TypeStartMod.MOD_ID, "choice_result");

    public static void registerServerPackets() {

        // Quando jogador entra no servidor, verifica se precisa escolher tipo
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            String uuid = player.getUuidAsString();

            if (!TypeDataManager.hasChosen(uuid)) {
                // Manda pacote para abrir a tela no cliente
                server.execute(() -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    ServerPlayNetworking.send(player, OPEN_TYPE_SCREEN, buf);
                });
            }
        });

        // Recebe a escolha de tipo do cliente
        ServerPlayNetworking.registerGlobalReceiver(TYPE_CHOSEN, (server, player, handler, buf, responseSender) -> {
            String chosenType = buf.readString();
            String uuid = player.getUuidAsString();

            server.execute(() -> {
                PacketByteBuf response = PacketByteBufs.create();

                // Verifica se o tipo já foi pego
                if (TypeDataManager.isTypeTaken(chosenType)) {
                    String ownerUuid = TypeDataManager.getTypeOwner(chosenType);
                    String ownerName = "outro jogador";
                    var ownerPlayer = server.getPlayerManager().getPlayer(java.util.UUID.fromString(ownerUuid));
                    if (ownerPlayer != null) ownerName = ownerPlayer.getName().getString();

                    response.writeBoolean(false); // falhou
                    response.writeString("O tipo " + StarterTypes.TYPE_DISPLAY_NAMES.get(chosenType)
                            + " ja foi escolhido por " + ownerName + "!");
                    ServerPlayNetworking.send(player, CHOICE_RESULT, response);
                    return;
                }

                // Tipo disponivel — manda a lista de pokemons desse tipo para o cliente escolher
                response.writeBoolean(true); // sucesso
                response.writeString(chosenType);
                List<String> pokemons = StarterTypes.TYPE_STARTERS.get(chosenType);
                response.writeInt(pokemons.size());
                for (String poke : pokemons) response.writeString(poke);

                ServerPlayNetworking.send(player, CHOICE_RESULT, response);
            });
        });

        // Recebe o Pokemon escolhido pelo cliente
        ServerPlayNetworking.registerGlobalReceiver(POKEMON_CHOSEN, (server, player, handler, buf, responseSender) -> {
            String chosenType    = buf.readString();
            String chosenPokemon = buf.readString();
            String uuid = player.getUuidAsString();

            server.execute(() -> {
                // Ultima verificacao de seguranca: tipo ainda disponivel?
                if (TypeDataManager.isTypeTaken(chosenType) && !TypeDataManager.getTypeOwner(chosenType).equals(uuid)) {
                    player.sendMessage(Text.literal("§cErro: o tipo ja foi pego enquanto voce escolhia!"), false);
                    // Reabre a tela de tipo
                    PacketByteBuf buf2 = PacketByteBufs.create();
                    ServerPlayNetworking.send(player, OPEN_TYPE_SCREEN, buf2);
                    return;
                }

                // Registra a escolha
                TypeDataManager.registerChoice(uuid, chosenType);
                TypeDataManager.save(server);

                // Entrega o Pokemon via API do Cobblemon
                var species = PokemonSpecies.INSTANCE.getByName(chosenPokemon);
                if (species != null) {
                    Pokemon pokemon = species.create(5); // nivel 5
                    com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party =
                        com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player);
                    party.add(pokemon);
                    player.sendMessage(Text.literal("§aVoce recebeu um " + capitalize(chosenPokemon) + "! Boa sorte no campeonato!"), false);
                } else {
                    player.sendMessage(Text.literal("§cErro: Pokemon nao encontrado. Contate um admin."), false);
                }
            });
        });
    }

    public static void registerClientPackets() {

        // Recebe ordem do servidor para abrir a tela de tipo
        ClientPlayNetworking.registerGlobalReceiver(OPEN_TYPE_SCREEN, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.setScreen(new TypeSelectScreen(null));
            });
        });

        // Recebe resultado da escolha de tipo
        ClientPlayNetworking.registerGlobalReceiver(CHOICE_RESULT, (client, handler, buf, responseSender) -> {
            boolean success = buf.readBoolean();

            if (!success) {
                String errorMsg = buf.readString();
                client.execute(() -> {
                    // Mostra erro e reabre tela de tipo
                    client.setScreen(new TypeSelectScreen(errorMsg));
                });
            } else {
                String chosenType = buf.readString();
                int count = buf.readInt();
                String[] pokemons = new String[count];
                for (int i = 0; i < count; i++) pokemons[i] = buf.readString();

                client.execute(() -> {
                    client.setScreen(new com.typestart.screen.PokemonSelectScreen(chosenType, pokemons));
                });
            }
        });
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
