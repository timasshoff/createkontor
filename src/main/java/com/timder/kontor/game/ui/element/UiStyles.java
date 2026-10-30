package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.BasicStyle;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public class UiStyles {

    private static final ResourceLocation KONTOR =
            ResourceLocation.fromNamespaceAndPath("createkontor", "lss/kontor.lss");
    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath("createkontor", "textures/gui/kontor_gui.png");

    private static final SpriteTexture PANEL_TEXTURE =
            SpriteTexture.of(GUI).setSprite(1, 1, 16, 16).setBorder(5, 5, 5, 5);
    private static final SpriteTexture INSET_TEXTURE =
            SpriteTexture.of(GUI).setSprite(19, 1, 16, 16).setBorder(3, 3, 3, 3);

    public static final Consumer<BasicStyle> PANEL = style -> style.background(PANEL_TEXTURE);
    public static final Consumer<BasicStyle> CARD = style -> style.background(INSET_TEXTURE);
    public static final Consumer<BasicStyle> TRANSPARENT = style -> style.background(IGuiTexture.EMPTY);
    public static final Consumer<BasicStyle> DIVIDER = style -> style.background(new ColorRectTexture(0xFF555555));

    static {
        PANEL_TEXTURE.setWrapMode(SpriteTexture.WrapMode.REPEAT);
    }

    public static UI ui(UIElement root) {
        return UI.of(root, KONTOR);
    }
}
