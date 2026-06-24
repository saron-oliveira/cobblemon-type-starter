package com.typestart.screen;

import com.typestart.data.StarterTypes;
import com.typestart.network.TypeStartNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

/**
 * Tela de escolha de tipo.
 * Exibe os 18 tipos em grade 3x6 com as cores de cada tipo.
 */
public class TypeSelectScreen extends Screen {

    private final String errorMessage; // null se sem erro

    // Dimensoes dos botoes de tipo
    private static final int BTN_W = 90;
    private static final int BTN_H = 20;
    private static final int COLS   = 3;
    private static final int GAP_X  = 8;
    private static final int GAP_Y  = 6;

    public TypeSelectScreen(String errorMessage) {
        super(Text.literal("Escolha o seu Tipo"));
        this.errorMessage = errorMessage;
    }

    @Override
    protected void init() {
        super.init();

        int totalW = COLS * BTN_W + (COLS - 1) * GAP_X;
        int startX = (this.width  - totalW) / 2;

        int rows   = (int) Math.ceil(StarterTypes.TYPE_ORDER.size() / (double) COLS);
        int totalH = rows * BTN_H + (rows - 1) * GAP_Y;
        int startY = (this.height - totalH) / 2 + 20; // +20 para dar espaco ao titulo

        for (int i = 0; i < StarterTypes.TYPE_ORDER.size(); i++) {
            String type = StarterTypes.TYPE_ORDER.get(i);
            String displayName = StarterTypes.TYPE_DISPLAY_NAMES.get(type);

            int col = i % COLS;
            int row = i / COLS;
            int x   = startX + col * (BTN_W + GAP_X);
            int y   = startY + row * (BTN_H + GAP_Y);

            final String typeFinal = type;
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(displayName),
                btn -> sendTypeChoice(typeFinal)
            ).dimensions(x, y, BTN_W, BTN_H).build());
        }
    }

    private void sendTypeChoice(String type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(type);
        ClientPlayNetworking.send(TypeStartNetwork.TYPE_CHOSEN, buf);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fundo escuro semitransparente
        this.renderBackground(context, mouseX, mouseY, delta);

        // Titulo
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§l§eEscolha o Tipo do seu Pokemon Inicial"),
            this.width / 2,
            16,
            0xFFFFFF
        );

        // Subtitulo
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Cada tipo so pode ser escolhido por um jogador"),
            this.width / 2,
            28,
            0xAAAAAA
        );

        // Mensagem de erro (se houver)
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§c" + errorMessage),
                this.width / 2,
                42,
                0xFF5555
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // Nao permite fechar com ESC — o jogador PRECISA escolher
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
