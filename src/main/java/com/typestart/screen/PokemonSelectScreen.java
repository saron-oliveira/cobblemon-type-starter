package com.typestart.screen;

import com.typestart.network.TypeStartNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

/**
 * Tela de escolha do Pokemon dentro do tipo escolhido.
 * Aparece depois que o servidor confirma que o tipo estava disponivel.
 */
public class PokemonSelectScreen extends Screen {

    private final String chosenType;
    private final String[] pokemons;

    private static final int BTN_W = 120;
    private static final int BTN_H = 24;
    private static final int GAP   = 8;

    public PokemonSelectScreen(String chosenType, String[] pokemons) {
        super(Text.literal("Escolha o seu Pokemon"));
        this.chosenType = chosenType;
        this.pokemons   = pokemons;
    }

    @Override
    protected void init() {
        super.init();

        int totalH = pokemons.length * BTN_H + (pokemons.length - 1) * GAP;
        int startX = (this.width  - BTN_W) / 2;
        int startY = (this.height - totalH) / 2 + 10;

        for (int i = 0; i < pokemons.length; i++) {
            final String pokemon = pokemons[i];
            String displayName = capitalize(pokemon);
            int y = startY + i * (BTN_H + GAP);

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§a" + displayName),
                btn -> sendPokemonChoice(pokemon)
            ).dimensions(startX, y, BTN_W, BTN_H).build());
        }

        // Botao de voltar para a escolha de tipo
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7< Voltar"),
            btn -> this.client.setScreen(new TypeSelectScreen(null))
        ).dimensions((this.width - 80) / 2, this.height - 40, 80, 20).build());
    }

    private void sendPokemonChoice(String pokemon) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(chosenType);
        buf.writeString(pokemon);
        ClientPlayNetworking.send(TypeStartNetwork.POKEMON_CHOSEN, buf);
        this.client.setScreen(null); // fecha a tela
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§l§eEscolha o seu Pokemon Inicial"),
            this.width / 2, 16, 0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Tipo escolhido: §f" + capitalize(chosenType)),
            this.width / 2, 28, 0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
