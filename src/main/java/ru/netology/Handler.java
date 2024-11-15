package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

// функциональный интерфейс для обработки запроса
@FunctionalInterface
public interface Handler {
    // метод для обработки запроса
    void handle (Request request, BufferedOutputStream outputStream) throws IOException;
    }
