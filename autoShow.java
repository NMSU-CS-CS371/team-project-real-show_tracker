import java.util.Scanner;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class autoShow {
    private static final String API_KEY = "2b0f7839577335a21b806dc74d1375efc188a593077741c269a639e7d6006e74";

    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);

        System.out.println("Select Media Choice: ");
        System.out.print("1. Movie" + "\n2. TV Show" + "\n3. Anime" + "\n\nCHOICE: ");
        int choice = input.nextInt();
        input.nextLine();

        while (choice < 1 || choice > 3){
            System.out.print("ERROR: Choice must be int between 1-3, try again" + "\nCHOICE:");
            choice = input.nextInt();
            input.nextLine();
        }

        String name = "";
        if (choice == 1){ 
            System.out.print("\nEnter Movie Name: ");
            name = input.nextLine();
            searchMovie(name);
        }
        if (choice == 2){
            System.out.print("\nEnter Show Name: ");
            name = input.nextLine();
            searchShow(name);
        }
        if (choice == 3){
            System.out.print("\nNot Yet Implemented...");
        }

    }


    public static void searchMovie(String query) throws Exception {
        Scanner input = new Scanner(System.in);

        // Encode the search string
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.simkl.com/search/movie?q=" + encodedQuery;


        // HTTP JAVA DOCUMENTATION: https://docs.oracle.com/en/java/javase/23/docs/api/java.net.http/java/net/http/HttpClient.html
        HttpClient client = HttpClient.newHttpClient(); // Create HTTP client

        // Builds request with the header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("simkl-api-key", API_KEY)
                .GET()
                .build();

        // sends the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Checks status
        if (response.statusCode() == 200) {

            JSONArray results = new JSONArray(response.body());
            System.out.println("\n\n\nRESULTS RESULTS RESULTS RESULTS RESULTS\n\n\n" + results + "\n\n\n\n\n\n\n\n\n\n");

            if (results.length() == 0) {
                System.out.println("No movies found.");
                return;
            }

            // Loop through results
            for (int i = 0; i < results.length(); i++) {
                JSONObject movieObj = results.getJSONObject(i);

                String title = movieObj.optString("title", "Unknown Title");
                int year = movieObj.optInt("year", 0);
                int id = movieObj.getJSONObject("ids").getInt("simkl_id");

                System.out.println((i + 1) + " - " + title + " (" + year + ")" + "     (SIMKL_ID: " + id + ")");
            }
            System.out.print("\nCHOICE (0 if movie is not listed): ");
            int choice = input.nextInt();
            input.nextLine(); // consume newline

            // make sure choice is in valid range based on number of results
            while (choice < 0 || choice > results.length()){
                System.out.print("ERROR: Choice must be int between 0-" + results.length() + ", try again" + "\nCHOICE:");
                choice = input.nextInt();
                input.nextLine();
            }

            if (choice == 0) {
                System.out.println("Movie not selected.");
                return;
            }

            JSONObject selectedMovie = results.getJSONObject(choice - 1);
            createMovieObj(selectedMovie);
            

        } else {
            System.out.println("Error: " + response.statusCode() + " ☹");
        }
    }





    public static void searchShow(String query) throws Exception {
        Scanner input = new Scanner(System.in);

        // Encode the search string
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.simkl.com/search/tv?q=" + encodedQuery;


        // HTTP JAVA DOCUMENTATION: https://docs.oracle.com/en/java/javase/23/docs/api/java.net.http/java/net/http/HttpClient.html
        HttpClient client = HttpClient.newHttpClient(); // Create HTTP client

        // Builds request with the header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("simkl-api-key", API_KEY)
                .GET()
                .build();

        // sends the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Checks status
        if (response.statusCode() == 200) {

            JSONArray results = new JSONArray(response.body());
            System.out.println("\n\n\nRESULTS RESULTS RESULTS RESULTS RESULTS\n\n\n" + results + "\n\n\n\n\n\n\n\n\n\n");

            if (results.length() == 0) {
                System.out.println("No shows found.");
                return;
            }

            // Loop through results
            for (int i = 0; i < results.length(); i++) {
                JSONObject showObj = results.getJSONObject(i);

                String title = showObj.optString("title", "Unknown Title");
                int year = showObj.optInt("year", 0);
                int id = showObj.getJSONObject("ids").getInt("simkl_id");

                System.out.println((i + 1) + " - " + title + " (" + year + ")" + "     (SIMKL_ID: " + id + ")");
            }
            System.out.print("\nCHOICE (0 if movie is not listed): ");
            int choice = input.nextInt();
            input.nextLine(); // consume newline

            // make sure choice is in valid range based on number of results
            while (choice < 0 || choice > results.length()){
                System.out.print("ERROR: Choice must be int between 0-" + results.length() + ", try again" + "\nCHOICE:");
                choice = input.nextInt();
                input.nextLine();
            }

            if (choice == 0) {
                System.out.println("Show not selected.");
                return;
            }

            JSONObject selectedShow = results.getJSONObject(choice - 1);
            createShowObj(selectedShow);
            

        } else {
            System.out.println("Error: " + response.statusCode() + " ☹");
        }
    }

    


    
    public static void createMovieObj(JSONObject movie) throws Exception{
        String title = movie.optString("title", "Unknown Title");
        int year = movie.optInt("year", 0);
        int id = movie.getJSONObject("ids").getInt("simkl_id");

        String url = "https://api.simkl.com/movies/" + id + "?extended=full";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("simkl-api-key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            //Stores more specific information about the movie than the search result stores
            JSONObject movieInfo = new JSONObject(response.body());

            System.out.println(movieInfo);

            // PULL RUNTIME (stores -1 if not found)
            int runtime = movieInfo.optInt("runtime", -1);

            // PULL DIRECTOR (stores "Unknown" if not found)
            String director = movieInfo.optString("director", "Unknown");


            Movie movieOBJ = new Movie(title, year, director, runtime, id);
            System.out.println(movieOBJ);

            

        } else {
            System.out.println("Error getting movie details. ☹");
        }
    }



    public static void createShowObj(JSONObject show) throws Exception {
        String title = show.optString("title", "Unknown Title");
        int year = show.optInt("year", 0);
        int id = show.getJSONObject("ids").getInt("simkl_id");

        HttpClient client = HttpClient.newHttpClient();

        // Get detailed show info
        String url = "https://api.simkl.com/tv/" + id + "?extended=full";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("simkl-api-key", API_KEY)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject showInfo = new JSONObject(response.body());
            System.out.println(showInfo);

            // Get seasons info
            String seasonsUrl = "https://api.simkl.com/tv/" + id + "/seasons";
            HttpRequest seasonsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(seasonsUrl))
                    .header("simkl-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> seasonsResponse = client.send(seasonsRequest, HttpResponse.BodyHandlers.ofString());

            int[] episodesPerSeason = new int[0];

            if (seasonsResponse.statusCode() == 200) {
                String body = seasonsResponse.body().trim();
                if (body.startsWith("[")) {
                    JSONArray seasonsArray = new JSONArray(body);
                    episodesPerSeason = new int[seasonsArray.length()];
                    for (int i = 0; i < seasonsArray.length(); i++) {
                        JSONObject season = seasonsArray.getJSONObject(i);
                        episodesPerSeason[i] = season.optInt("episode_count", 0);
                    }
                } else {
                    System.out.println("\nSeasons info not available or not in array format. Falling back to total_episodes.");
                    // fallback: put all episodes in a single season
                    int totalEpisodes = showInfo.optInt("total_episodes", 0);
                    if (totalEpisodes > 0) {
                        episodesPerSeason = new int[] { totalEpisodes };
                    }
                }
            
            } else {
                System.out.println("Error getting seasons info. Status: " + seasonsResponse.statusCode());
            }

            int seasons = episodesPerSeason.length;
            Show showOBJ = new Show(title, year, seasons, episodesPerSeason, id);
            System.out.println(showOBJ);

        } else {
            System.out.println("Error getting show details. Status: " + response.statusCode());
        }
    }
}
