package com.typestart;

import net.fabricmc.api.ClientModInitializer;

/**
 * Inicializador do lado cliente.
 *
 * A escolha de inicial agora usa a GUI NATIVA do Cobblemon, entao nao ha telas
 * nem pacotes proprios para registrar aqui. A classe permanece (entrypoint
 * "client" no fabric.mod.json) caso seja preciso adicionar algo no futuro.
 */
public class TypeStartClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Nada a fazer no cliente — o Cobblemon cuida da interface.
    }
}
