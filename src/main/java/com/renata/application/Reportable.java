package com.renata.application;

import java.util.function.Predicate;

/** Інтерфейс для сервісів, які генерують звіти на основі певних умов. */
public interface Reportable<E> {

    void generateReport(Predicate<E> predicate);
}
