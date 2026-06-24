package com.typestart;

import com.typestart.command.TypeStartCommands;
import com.typestart.data.TypeDataManager;
import com.typestart.network.TypeStartNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeStartMod implements ModInitializer {

    public static final String MOD_ID = "typestart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Cobblemon Type Starter carregado!");

        // Registra os pacotes de rede (comunicacao cliente-servidor)
        TypeStartNetwork.registerPayloads();
        TypeStartNetwork.registerServerPackets();

        // Registra os comandos admin
        TypeStartCommands.register();

        // Salva os dados quando o servidor para
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TypeDataManager.save(server);
        });

        // Carrega os dados quando o servidor inicia
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TypeDataManager.load(server);
        });
    }
}
