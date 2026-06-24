package com.typestart.network;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.typestart.TypeStartMod;
import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import com.typestart.screen.TypeSelectScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public class TypeStartNetwork {

    public record OpenTypeScreenPayload() implements CustomPayload {
        public static final Id<OpenTypeScreenPayload> ID = new Id<>(Identifier.of(TypeStartMod.MOD_ID, "open_type_screen"));
        public static final PacketCodec<RegistryByteBuf, OpenTypeScreenPayload> CODEC = PacketCodec.unit(new OpenTypeScreenPayload());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record TypeChosenPayload(String type) implements CustomPayload {
        public static final Id<TypeChosenPayload> ID = new Id<>(Identifier.of(TypeStartMod.MOD_ID, "type_chosen"));
        public static final PacketCodec<RegistryByteBuf, TypeChosenPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, TypeChosenPayload::type, TypeChosenPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record PokemonChosenPayload(String type, String pokemon) implements CustomPayload {
        public static final Id<PokemonChosenPayload> ID = new Id<>(Identifier.of(TypeStartMod.MOD_ID, "pokemon_chosen"));
        public static final PacketCodec<RegistryByteBuf, PokemonChosenPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, PokemonChosenPayload::type, PacketCodecs.STRING, PokemonChosenPayload::pokemon, PokemonChosenPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ChoiceResultPayload(boolean success, String data1, String data2) implements CustomPayload {
        public static final Id<ChoiceResultPayload> ID = new Id<>(Identifier.of(TypeStartMod.MOD_ID, "choice_result"));
        public static final PacketCodec<RegistryByteBuf, ChoiceResultPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, ChoiceResultPayload::success, PacketCodecs.STRING, ChoiceResultPayload::data1, PacketCodecs.STRING, ChoiceResultPayload::data2, ChoiceResultPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(OpenTypeScreenPayload.ID, OpenTypeScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TypeChosenPayload.ID, TypeChosenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PokemonChosenPayload.ID, PokemonChosenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChoiceResultPayload.ID, ChoiceResultPayload.CODEC);
    }

    public static void registerServerPackets() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (!TypeDataManager.hasChosen(player.getUuidAsString())) {
                ServerPlayNetworking.send(player, new OpenTypeScreenPayload());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TypeChosenPayload.ID, (payload, context) -> {
            String chosenType = payload.type();
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (TypeDataManager.isTypeTaken(chosenType)) {
                    String ownerUuid = TypeDataManager.getTypeOwner(chosenType);
                    String ownerName = "outro jogador";
                    var ownerPlayer = context.server().getPlayerManager().getPlayer(UUID.fromString(ownerUuid));
                    if (ownerPlayer != null) ownerName = ownerPlayer.getName().getString();
                    String msg = "O tipo " + StarterTypes.TYPE_DISPLAY_NAMES.get(chosenType) + " ja foi escolhido por " + ownerName + "!";
                    ServerPlayNetworking.send(player, new ChoiceResultPayload(false, msg, ""));
                    return;
                }
                List<String> pokemons = StarterTypes.TYPE_STARTERS.get(chosenType);
                String pokes = String.join(",", pokemons);
                ServerPlayNetworking.send(player, new ChoiceResultPayload(true, chosenType, pokes));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PokemonChosenPayload.ID, (payload, context) -> {
            String chosenType = payload.type();
            String chosenPokemon = payload.pokemon();
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (TypeDataManager.isTypeTaken(chosenType) && !TypeDataManager.getTypeOwner(chosenType).equals(player.getUuidAsString())) {
                    player.sendMessage(Text.literal("§cErro: o tipo ja foi pego enquanto voce escolhia!"), false);
                    ServerPlayNetworking.send(player, new OpenTypeScreenPayload());
                    return;
                }
                TypeDataManager.registerChoice(player.getUuidAsString(), chosenType);
                TypeDataManager.save(context.server());

                var species = PokemonSpecies.INSTANCE.getByName(chosenPokemon.toLowerCase());
                if (species != null) {
                    Pokemon pokemon = species.create(5);
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
        ClientPlayNetworking.registerGlobalReceiver(OpenTypeScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> context.client().setScreen(new TypeSelectScreen(null)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ChoiceResultPayload.ID, (payload, context) -> {
            if (!payload.success()) {
                context.client().execute(() -> context.client().setScreen(new TypeSelectScreen(payload.data1())));
            } else {
                String chosenType = payload.data1();
                String[] pokemons = payload.data2().split(",");
                context.client().execute(() -> context.client().setScreen(new com.typestart.screen.PokemonSelectScreen(chosenType, pokemons)));
            }
        });
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
