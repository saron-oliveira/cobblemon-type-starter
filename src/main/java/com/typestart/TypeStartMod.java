package com.typestart;

import com.typestart.command.TypeStartCommands;
import com.typestart.compat.CobblemonStarter;
import com.typestart.data.TypeDataManager;
import com.typestart.util.TabName;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeStartMod implements ModInitializer {

    public static final String MOD_ID = "typestart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Cobblemon Type Starter carregado!");

        // Registra os comandos admin
        TypeStartCommands.register();

        // Salva os dados quando o servidor para
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TypeDataManager.save(server);
        });

        // Carrega os dados e configura a integracao com o Cobblemon ao iniciar.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TypeDataManager.load(server);
            // Configura a GUI nativa do Cobblemon com os nossos tipos e ativa a
            // regra "um tipo por jogador". A escolha passa a ser pela tela nativa.
            CobblemonStarter.setup();
        });

        // Ao entrar, reaplica a tag [Tipo] na lista TAB de quem ja escolheu.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            if (TypeDataManager.hasChosen(player.getUuidAsString())) {
                TabName.apply(player);
            }
        });
    }
}
