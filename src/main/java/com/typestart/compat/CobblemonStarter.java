package com.typestart.compat;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.player.GeneralPlayerData;
import com.cobblemon.mod.common.config.starter.StarterCategory;
import com.cobblemon.mod.common.config.starter.StarterConfig;
import com.typestart.TypeStartMod;
import com.typestart.data.StarterTypes;
import com.typestart.data.TypeDataManager;
import com.typestart.util.TabName;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Integracao com a GUI NATIVA de iniciais do Cobblemon.
 *
 * Em vez de telas proprias, o mod usa a tela bonita do proprio Cobblemon, mas:
 *   1) configura a lista de iniciais como os NOSSOS 18 tipos (cada tipo vira uma
 *      "categoria" com os Pokemon que listamos) — {@link #configureStarters()};
 *   2) ouve o evento de escolha ({@code STARTER_CHOSEN}) para impor a regra de
 *      campeonato "um tipo por jogador": se o tipo ja foi pego por outro, cancela
 *      a escolha e reabre a tela — {@link #handleStarterChosen}.
 *
 * Como a escolha passa pelo fluxo nativo, o proprio Cobblemon marca o jogador como
 * "ja escolheu" e o aviso "Press M" some sozinho.
 *
 * Todas as chamadas a API do Cobblemon sao protegidas com try/catch para nao
 * derrubar o mod caso a API mude em outra versao.
 */
public final class CobblemonStarter {

    private CobblemonStarter() {}

    private static boolean subscribed = false;

    // Nivel com que todos os iniciais sao entregues.
    private static final int STARTER_LEVEL = 5;

    /** Chamado quando o servidor inicia: configura os iniciais e ouve as escolhas. */
    public static void setup() {
        configureStarters();
        subscribeStarterChosen();
    }

    /** Substitui a lista de iniciais do Cobblemon pelos nossos 18 tipos. */
    private static void configureStarters() {
        try {
            List<StarterCategory> categories = new ArrayList<>();
            for (String type : StarterTypes.TYPE_ORDER) {
                String displayName = StarterTypes.TYPE_DISPLAY_NAMES.getOrDefault(type, type);
                List<PokemonProperties> mons = new ArrayList<>();
                for (String species : StarterTypes.TYPE_STARTERS.getOrDefault(type, List.of())) {
                    // "charmander" -> PokemonProperties(species=charmander)
                    PokemonProperties props = PokemonProperties.Companion.parse(species, " ", "=");
                    props.setLevel(STARTER_LEVEL); // garante que todos venham no nivel 5
                    mons.add(props);
                }
                categories.add(new StarterCategory(type, displayName, mons));
            }

            StarterConfig cfg = Cobblemon.INSTANCE.getStarterConfig();
            cfg.setStarters(categories);
            cfg.setUseConfigStarters(true);
            cfg.setAllowStarterOnJoin(true);
            cfg.setPromptStarterOnceOnly(false);
            TypeStartMod.LOGGER.info("Iniciais do Cobblemon configurados com os {} tipos do TypeStart.", categories.size());
        } catch (Throwable t) {
            TypeStartMod.LOGGER.warn("Nao foi possivel configurar os iniciais do Cobblemon: {}", t.toString());
        }
    }

    /** Registra o ouvinte do evento de escolha (so uma vez). */
    private static void subscribeStarterChosen() {
        if (subscribed) return;
        try {
            Consumer<StarterChosenEvent> handler = CobblemonStarter::handleStarterChosen;
            CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.HIGHEST, handler);
            subscribed = true;
        } catch (Throwable t) {
            TypeStartMod.LOGGER.warn("Nao foi possivel registrar o evento de escolha de inicial: {}", t.toString());
        }
    }

    /** Aplica a regra "um tipo por jogador" e registra a escolha. */
    private static void handleStarterChosen(StarterChosenEvent event) {
        try {
            ServerPlayerEntity player = event.getPlayer();
            String species = event.getPokemon().getSpecies().showdownId();
            String type = StarterTypes.typeForPokemon(species);
            if (type == null) {
                return; // nao eh um dos nossos iniciais; deixa o Cobblemon seguir
            }

            String uuid = player.getUuidAsString();
            String owner = TypeDataManager.getTypeOwner(type);

            if (owner != null && !owner.equals(uuid)) {
                // Tipo ja pego por outro jogador -> cancela e reabre a tela.
                event.cancel();
                String typeName = StarterTypes.TYPE_DISPLAY_NAMES.getOrDefault(type, type);
                player.sendMessage(Text.literal("§cO tipo " + typeName + " ja foi escolhido! Escolha outro."), false);
                var server = player.getServer();
                if (server != null) {
                    server.execute(() -> reprompt(player));
                }
                return;
            }

            // Escolha valida: registra o tipo e aplica a tag [Tipo] na lista TAB.
            TypeDataManager.registerChoice(uuid, type);
            var server = player.getServer();
            if (server != null) {
                TypeDataManager.save(server);
            }
            TabName.apply(player);
        } catch (Throwable t) {
            TypeStartMod.LOGGER.warn("Erro ao processar escolha de inicial: {}", t.toString());
        }
    }

    /** Reabre a tela nativa de escolha de inicial para o jogador. */
    public static void reprompt(ServerPlayerEntity player) {
        try {
            Cobblemon.INSTANCE.getStarterHandler().requestStarterChoice(player);
        } catch (Throwable t) {
            TypeStartMod.LOGGER.warn("Nao foi possivel reabrir a tela de inicial: {}", t.toString());
        }
    }

    /**
     * Reseta os dados de inicial do Cobblemon do jogador (usado nos comandos admin),
     * para que ele possa escolher de novo pela tela nativa.
     */
    public static void resetPlayerData(ServerPlayerEntity player) {
        try {
            GeneralPlayerData data = Cobblemon.INSTANCE.getPlayerDataManager().getGenericData(player);
            data.setStarterSelected(false);
            data.setStarterLocked(false);
            data.setStarterPrompted(false);
            data.sendToPlayer(player);
        } catch (Throwable t) {
            TypeStartMod.LOGGER.warn("Nao foi possivel resetar os dados de inicial do Cobblemon: {}", t.toString());
        }
    }
}
