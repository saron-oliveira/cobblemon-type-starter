package com.typestart;

import com.typestart.network.TypeStartNetwork;
import net.fabricmc.api.ClientModInitializer;

public class TypeStartClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Registra os pacotes que o cliente recebe do servidor
        TypeStartNetwork.registerClientPackets();
    }
}
