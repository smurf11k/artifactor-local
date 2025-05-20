package com.renata.presentation.util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;

/** Завантажує FXML-файли з інтеграцією Spring (автоматичне зв'язування контролерів через Spring context). */
public class SpringFXMLLoader {

    private final ApplicationContext context;

    /** Ініціалізує завантажувач з Spring контекстом для DI контролерів. */
    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    /** Завантажує FXML-файл, інжектячи Spring-керовані контролери. */
    public Object load(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }
}
