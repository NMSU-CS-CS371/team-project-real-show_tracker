import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;

public class webServ {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                serveFile(exchange, "index2.html", "text/html");
            }
        });

        server.createContext("/style.css", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                serveFile(exchange, "style.css", "text/css");
            }
        });

        server.createContext("/app2.js", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                serveFile(exchange, "app2.js", "application/javascript");
            }
        });

        server.createContext("/search", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String query = exchange.getRequestURI().getQuery();
                String searchTerm = null;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] parts = param.split("=", 2);
                        if (parts.length == 2 && "query".equals(parts[0])) {
                            searchTerm = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                if (searchTerm == null || searchTerm.isBlank()) {
                    sendJsonResponse(exchange, "{\"message\":\"Missing query parameter\"}", 400);
                    return;
                }

                try {
                    JSONArray results = autoShow.searchMovie(searchTerm);
                    sendJsonResponse(exchange, results.toString(), 200);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendJsonResponse(exchange, "{\"message\":\"Search error\"}", 500);
                }
            }
        });

        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    private static void serveFile(HttpExchange exchange, String filePath, String contentType) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String responseBody, int statusCode) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}