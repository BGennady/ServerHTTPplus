package ru.netology;


import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// класс разобрает строку запроса и предоставляет доступ к её частям: метод, путь, заголовки и тело.
public class Request {
    private final String method; // HTTP метод (GET, POST и т.д.)
    private final String path; // тип ресурса, к которому клиент хочет получить доступ (/messages)
    private final String queryString; // параметры запроса целиком (после "?")
    private final Map<String, List<String>> queryParams; // параметры запроса распарсеные и упакованые в Мапу

    public Request(String method, String path,String queryString) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.queryParams = parseQueryParams(queryString); // парсим queryString
    }

    // метод для получения параметров из Query String
    public Map<String, List<String>> parseQueryParams(String queryString) {
        if (queryString != null && !queryString.isEmpty()) {
            // использую готовый инструмент URLEncodedUtils для парсинга строки запроса
            // используется инструмет URLEncodedUtils для парсинга строки запроса
            return URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8).stream()
                    .collect(Collectors.groupingBy(pair -> pair.getName(), Collectors.mapping(pair ->
                            pair.getValue(), Collectors.toList())));
        }
        return Map.of();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    // метод получения параметра(значения) по имени(ключу)
    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
