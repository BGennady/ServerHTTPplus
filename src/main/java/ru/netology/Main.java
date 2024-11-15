package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // добавление обработчика для GET-запроса
        server.addHandler("GET", "/message", (request, outputStream) ->{
                    String response = "Ответ на запрос GET /messages";
            try {
                outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                outputStream.write(("Content-Length: " + response.length() + "\r\n").getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(response.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // добавление обработчика для POST-запроса
        server.addHandler("POST", "/submit", (request, outputStream) -> {
            // извлечение параметров из тела запроса
            String name = request.getPostparam("name");
            String email = request.getPostparam("email");
            // Формируем строку ответа
            String response = String.format("Name: %s, Email: %s", name, email);

            // Отправляем ответ
            outputStream.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + response).getBytes());
            outputStream.flush();
        });

       server.start(9999);
    }
}
