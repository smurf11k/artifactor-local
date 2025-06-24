package com.renata.presentation.util;

import atlantafx.base.theme.Styles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

/* Менеджер стилів для UI */
@Component
public class StyleManager {
    private StyleManager() {}

    public final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void applyTypeStyle(Text text, String type) {
        text.getStyleClass().clear();
        text.getStyleClass().add(Styles.TEXT);

        if (type != null) {
            switch (type) {
                case "LISTED":
                    text.getStyleClass().add(Styles.ACCENT);
                    break;
                case "RELISTED":
                    text.getStyleClass().add(Styles.WARNING);
                    break;
                case "PRICE_UPDATED":
                    text.getStyleClass().add(Styles.SUCCESS);
                    break;
                case "PURCHASED":
                    text.getStyleClass().add(Styles.DANGER);
                    break;
                default:
                    text.getStyleClass().add(Styles.TEXT_SUBTLE);
            }
        }
    }

    public StringConverter<LocalDate> getLocalDateStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return DATE_FORMAT.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    return LocalDate.parse(string, DATE_FORMAT);
                } else {
                    return null;
                }
            }
        };
    }
}
