import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class webServ {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new StaticFileHandler("index2.html", "text/html"));
        server.createContext("/style.css", new StaticFileHandler("style.css", "text/css"));
        server.createContext("/app2.js", new StaticFileHandler("app2.js", "application/javascript"));

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

        server.createContext("/watchlist/load", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                try {
                    JSONArray entries = list.loadEntries();
                    sendJsonResponse(exchange, entries.toString(), 200);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendJsonResponse(exchange, "{\"message\":\"Unable to load watchlist\"}", 500);
                }
            }
        });

        server.createContext("/watchlist/save", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    JSONObject payload = new JSONObject(requestBody);
                    Movie savedMovie;

                    if (payload.has("ids") && payload.getJSONObject("ids").has("simkl_id")) {
                        // From search
                        savedMovie = autoShow.createMovieObj(payload);
                        if (savedMovie == null) {
                            sendJsonResponse(exchange, "{\"message\":\"Could not fetch movie details\"}", 500);
                            return;
                        }
                    } else {
                        // Manual entry
                        String title = payload.optString("title", "Unknown Title");
                        int year = payload.optInt("year", 0);
                        String director = payload.optString("director", "Unknown");
                        int runtime = payload.optInt("runtime_minutes", payload.optInt("runtime", 0));
                        savedMovie = new Movie(title, year, director, runtime, 0, ""); // simkl_id = 0 for manual
                    }

                    list.saveEntry(savedMovie);
                    sendJsonResponse(exchange, "{\"message\":\"Saved to watchlist\"}", 200);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendJsonResponse(exchange, "{\"message\":\"Save failed\"}", 500);
                }
            }
        });

        server.createContext("/watchlist/remove", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                int simklId = extractIntQueryParameter(exchange.getRequestURI(), "simkl_id");
                if (simklId <= 0) {
                    sendJsonResponse(exchange, "{\"message\":\"Missing simkl_id\"}", 400);
                    return;
                }

                try {
                    boolean removed = list.deleteEntry(simklId);
                    if (removed) {
                        sendJsonResponse(exchange, "{\"message\":\"Removed from watchlist\"}", 200);
                    } else {
                        sendJsonResponse(exchange, "{\"message\":\"Item not found\"}", 404);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendJsonResponse(exchange, "{\"message\":\"Remove failed\"}", 500);
                }
            }
        });

        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    private static int extractIntQueryParameter(URI uri, String name) {
        String query = uri.getQuery();
        if (query == null) {
            return -1;
        }
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) {
                try {
                    return Integer.parseInt(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return -1;
    }

    private static void sendJsonResponse(HttpExchange exchange, String responseBody, int statusCode) throws IOException {
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final String filePath;
        private final String contentType;

        StaticFileHandler(String filePath, String contentType) {
            this.filePath = filePath;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            serveFile(exchange, filePath, contentType);
        }
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
}