package com.typestart.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/**
 * Gerencia quais jogadores pegaram quais tipos.
 * Os dados são salvos em um arquivo JSON na pasta do mundo.
 */
public class TypeDataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "typestart_data.json";

    // UUID do jogador -> tipo escolhido (ex: "fire")
    private static Map<String, String> playerTypes = new HashMap<>();

    // Tipo -> UUID do jogador que pegou
    private static Map<String, String> takenTypes = new HashMap<>();

    public static void load(MinecraftServer server) {
        Path path = getFilePath(server);
        if (!path.toFile().exists()) return;

        try (Reader reader = new FileReader(path.toFile())) {
            Type mapType = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
            Map<String, Map<String, String>> data = GSON.fromJson(reader, mapType);
            if (data != null) {
                playerTypes = data.getOrDefault("playerTypes", new HashMap<>());
                takenTypes  = data.getOrDefault("takenTypes",  new HashMap<>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(MinecraftServer server) {
        Path path = getFilePath(server);
        try (Writer writer = new FileWriter(path.toFile())) {
            Map<String, Map<String, String>> data = new HashMap<>();
            data.put("playerTypes", playerTypes);
            data.put("takenTypes",  takenTypes);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Verifica se um jogador já escolheu um tipo
    public static boolean hasChosen(String playerUuid) {
        return playerTypes.containsKey(playerUuid);
    }

    // Retorna o tipo escolhido pelo jogador (ou null)
    public static String getPlayerType(String playerUuid) {
        return playerTypes.get(playerUuid);
    }

    // Verifica se um tipo já foi pego por alguém
    public static boolean isTypeTaken(String type) {
        return takenTypes.containsKey(type);
    }

    // Retorna o UUID de quem pegou o tipo (ou null)
    public static String getTypeOwner(String type) {
        return takenTypes.get(type);
    }

    // Registra a escolha de um jogador
    public static void registerChoice(String playerUuid, String type) {
        playerTypes.put(playerUuid, type);
        takenTypes.put(type, playerUuid);
    }

    // Remove a escolha de um jogador (comando admin de reset individual)
    public static void resetPlayer(String playerUuid) {
        String type = playerTypes.remove(playerUuid);
        if (type != null) takenTypes.remove(type);
    }

    // Reseta tudo (novo campeonato)
    public static void resetAll() {
        playerTypes.clear();
        takenTypes.clear();
    }

    // Retorna mapa de tipo -> uuid para listagem admin
    public static Map<String, String> getAllTakenTypes() {
        return Collections.unmodifiableMap(takenTypes);
    }

    private static Path getFilePath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(FILE_NAME);
    }
}
