package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64; // количество потоков для обработки подключений
    private final ExecutorService threadPool; // пул потоков, используемый для многопоточной обработки запросов
    // Мапа для хранения Хэндлеров
    private final Map<String, Map<String, Handler>> handlers = new HashMap<>();

    // конструктор, инициализирует пул потоков
    public Server() {
        // пул потоков фиксированного размера для обработки запросов
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    // метод принимает: метод, путь и хандлер
    public void addHandler(String method, String path, Handler handler) {
        handlers
                .computeIfAbsent(method, k -> new HashMap<>())
                .put(path, handler);
    }

    // метод запускает сервер и начинает ожидать подключения по переданому порту
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Сервер запущен на порту: %d%n", port);

            // цикл для ожидания новых подключений
            while (true) {
                // Принимаем новое подключение
                Socket clientSocket = serverSocket.accept();
                // Передаем обработку подключения в пул потоков
                threadPool.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод для обработки конкретного подключения
    private void handleConnection(Socket socket) {
        try (
                // поток для чтения данных от клиента
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // поток для отправки данных клиенту
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // чтение первой строки запроса
            String requestLine = in.readLine();
            // сплитуем строку на части для извлечения метода, пути и версии HTTP
            String[] parts = requestLine.split(" ");
            // проверка, что запрос содержит три части (метод, путь и версию)
            if (parts.length != 3) {
                return;  // если нет, завершаю обработку
            }
            String method = parts[0];
            String path = parts[1];
            String queryString = null;

            // находим индекс начинала строки-параметров (после "?")
            int queryStartIndex = path.indexOf("?");
            if (queryStartIndex != -1) { // если "?" не найден indexOf вернул -1
                queryString = path.substring(queryStartIndex + 1); //вырезаем строку от "?"
                path = path.substring(0, queryStartIndex); // перезаписываем путь без параметров
            }

            // парсим заголовки для получения для получени длины тела
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }

            // получаем тела запроса (если оно есть)
            String body = null;
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);  // чтение тела в зависимости от Content-Length
                body = new String(bodyChars);          // преобразование в строку
            }

            //  объекта запроса
            Request request = new Request(method, path, queryString, body);
            // объект хандлера
            Handler handler = findHandler(method, path);
            // проверяем, что не пустой
            if (handler != null) {
                handler.handle(request, out);
            } else {
                // Если не найден, отправляем ошибку 404
                out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод для поиска хендлера из Мапы
    private Handler findHandler(String method, String path) {

        // ппытаемся получить из Мапы, хандлер для этого метода
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers != null) {
            return methodHandlers.get(path); // получаем подмапу по ключу-путь
        }
        return null;
    }
}
