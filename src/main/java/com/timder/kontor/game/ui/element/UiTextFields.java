package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public class UiTextFields {
    public static final class LabeledTextField extends UIElement {
        private final TextField textField;

        private LabeledTextField(TextField textField) {
            this.textField = textField;
        }

        public TextField getTextField() {
            return textField;
        }
    }

    public static LabeledTextField labeled(
            Component label,
            @Nullable Component placeholder,
            @Nullable Component tooltip,
            String initial,
            Consumer<String> onCommit) {
        TextField field = new TextField();
        if (placeholder != null) {
            field.textFieldStyle(style -> style.placeholder(placeholder));
        }
        field.layout(layout -> layout.widthPercent(100));
        field.setText(initial, false);

        String[] current = {initial};
        String[] committed = {initial.trim()};
        field.setTextResponder(text -> current[0] = text);
        field.addEventListener(UIEvents.BLUR, event -> {
            String text = current[0].trim();
            if (!text.equals(committed[0])) {
                committed[0] = text;
                onCommit.accept(text);
            }
        });

        Label labelElement = UiLabels.primary(label, Horizontal.LEFT);

        LabeledTextField row = new LabeledTextField(field);
        row.layout(
                layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(2).widthPercent(100));
        row.addChildren(labelElement, field);
        if (tooltip != null) {
            row.style(style -> style.tooltips(tooltip));
        }
        return row;
    }
}
