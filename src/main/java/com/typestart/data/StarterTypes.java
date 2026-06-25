package com.typestart.data;

import java.util.List;
import java.util.Map;

/**
 * Define os tipos disponíveis e qual Pokemon inicial cada tipo oferece.
 * Para adicionar ou mudar um Pokemon, edite o Map abaixo.
 */
public class StarterTypes {

    // Mapa de tipo -> lista de especies de Pokemon (nome interno do Cobblemon)
    public static final Map<String, List<String>> TYPE_STARTERS = Map.ofEntries(
        Map.entry("fire",     List.of("charmander", "cyndaquil", "torchic")),   // já tem no Cobblemon
        Map.entry("water",    List.of("squirtle", "totodile", "mudkip")),        // já tem no Cobblemon
        Map.entry("grass",    List.of("bulbasaur", "chikorita", "treecko")),     // já tem no Cobblemon
        Map.entry("normal",   List.of("eevee")),
        Map.entry("electric", List.of("pikachu")),
        Map.entry("ice",      List.of("frigibax")),
        Map.entry("rock",     List.of("larvitar")),
        Map.entry("ground",   List.of("sandile")),
        Map.entry("flying",   List.of("rookidee")),
        Map.entry("poison",   List.of("gastly")),
        Map.entry("bug",      List.of("scyther")),
        Map.entry("fighting", List.of("machop")),
        Map.entry("psychic",  List.of("abra")),
        Map.entry("ghost",    List.of("litwick")),
        Map.entry("dragon",   List.of("axew")),
        Map.entry("dark",     List.of("pawniard")),
        Map.entry("steel",    List.of("aron")),
        Map.entry("fairy",    List.of("ralts"))
    );

    // Nome de exibição de cada tipo em português
    public static final Map<String, String> TYPE_DISPLAY_NAMES = Map.ofEntries(
        Map.entry("fire",     "Fogo"),
        Map.entry("water",    "Água"),
        Map.entry("grass",    "Planta"),
        Map.entry("normal",   "Normal"),
        Map.entry("electric", "Elétrico"),
        Map.entry("ice",      "Gelo"),
        Map.entry("rock",     "Pedra"),
        Map.entry("ground",   "Terra"),
        Map.entry("flying",   "Voador"),
        Map.entry("poison",   "Veneno"),
        Map.entry("bug",      "Inseto"),
        Map.entry("fighting", "Lutador"),
        Map.entry("psychic",  "Psíquico"),
        Map.entry("ghost",    "Fantasma"),
        Map.entry("dragon",   "Dragão"),
        Map.entry("dark",     "Sombrio"),
        Map.entry("steel",    "Aço"),
        Map.entry("fairy",    "Fada")
    );

    // Cor de cada tipo (formato hexadecimal) para a interface
    public static final Map<String, Integer> TYPE_COLORS = Map.ofEntries(
        Map.entry("fire",     0xF08030),
        Map.entry("water",    0x6890F0),
        Map.entry("grass",    0x78C850),
        Map.entry("normal",   0xA8A878),
        Map.entry("electric", 0xF8D030),
        Map.entry("ice",      0x98D8D8),
        Map.entry("rock",     0xB8A038),
        Map.entry("ground",   0xE0C068),
        Map.entry("flying",   0xA890F0),
        Map.entry("poison",   0xA040A0),
        Map.entry("bug",      0xA8B820),
        Map.entry("fighting", 0xC03028),
        Map.entry("psychic",  0xF85888),
        Map.entry("ghost",    0x705898),
        Map.entry("dragon",   0x7038F8),
        Map.entry("dark",     0x705848),
        Map.entry("steel",    0xB8B8D0),
        Map.entry("fairy",    0xEE99AC)
    );

    // Ordem de exibição na tela (3 colunas x 6 linhas)
    public static final List<String> TYPE_ORDER = List.of(
        "fire", "water", "grass",
        "normal", "electric", "ice",
        "rock", "ground", "flying",
        "poison", "bug", "fighting",
        "psychic", "ghost", "dragon",
        "dark", "steel", "fairy"
    );

    /**
     * Descobre a qual tipo um Pokemon pertence (busca reversa no TYPE_STARTERS).
     * Usado quando o jogador escolhe pela GUI nativa do Cobblemon, para saber o
     * tipo a partir da especie escolhida. Retorna null se nao for um dos nossos.
     */
    public static String typeForPokemon(String species) {
        if (species == null) return null;
        for (Map.Entry<String, List<String>> entry : TYPE_STARTERS.entrySet()) {
            for (String poke : entry.getValue()) {
                if (poke.equalsIgnoreCase(species)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
