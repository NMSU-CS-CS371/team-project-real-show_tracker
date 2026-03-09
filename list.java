import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class list {
    public static void main(String[] args) throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the name of the show/movie:");
        String name = scan.nextLine();

        String abbreviation = generateAbbreviation(name);
        System.out.println("Abbreviation generated: " + abbreviation);

        String type = "";
        while (!type.equals("movie") && !type.equals("show")) {
            System.out.println("Is this a movie or a show? (movie/show)");
            type = scan.nextLine().toLowerCase().trim();
            if (!type.equals("movie") && !type.equals("show"))
                System.out.println("Please type exactly 'movie' or 'show'.");
        }

        String dateEntered = LocalDate.now().toString();
        ArrayList<Integer> seasonEpisodes = new ArrayList<>();
        int episodeLength = 0;

        if (type.equals("movie")) {
            System.out.println("How long is the movie? (in minutes)");
            episodeLength = getNonNegativeInt(scan);
            seasonEpisodes.add(1);
        } else {
            System.out.println("How many seasons are there?");
            int numSeasons = getNonNegativeInt(scan);

            for (int i = 1; i <= numSeasons; i++) {
                System.out.println("How many episodes in Season " + i + "?");
                seasonEpisodes.add(getNonNegativeInt(scan));
            }

            System.out.println("How long is each episode on average? (minutes)");
            System.out.println("Common lengths: 20, 30, 60, 120 - or enter any non-negative number:");
            episodeLength = getNonNegativeInt(scan);
        }

        String url = "jdbc:sqlite:shows.sql";
        try (Connection conn = DriverManager.getConnection(url)) {

            createTablesIfNeeded(conn);

            int showId = insertShow(conn, name, abbreviation, type, episodeLength, dateEntered);

            String firstSeasonLabel = type.equals("movie") ? "Movie" : "Season 1";
            insertSeason(conn, showId, firstSeasonLabel, seasonEpisodes.get(0));

            for (int i = 1; i < seasonEpisodes.size(); i++) {
                insertSeason(conn, showId, "Season " + (i + 1), seasonEpisodes.get(i));
            }
        }

        System.out.println("\n\"" + name + "\" saved to shows.db!");
        scan.close();
    }

    private static void createTablesIfNeeded(Connection conn) throws SQLException {
        String createShows =
            "CREATE TABLE IF NOT EXISTS shows (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL," +
            "  abbreviation TEXT," +
            "  type TEXT," +
            "  avg_episode_length_mins INTEGER," +
            "  date_entered TEXT," +
            "  last_updated TEXT" +
            ");";

        String createSeasons =
            "CREATE TABLE IF NOT EXISTS seasons (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  show_id INTEGER NOT NULL," +
            "  season_label TEXT," +
            "  episodes INTEGER," +
            "  watched INTEGER DEFAULT 0," +
            "  FOREIGN KEY (show_id) REFERENCES shows(id)" +
            ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createShows);
            stmt.execute(createSeasons);
        }
    }

    private static int insertShow(Connection conn, String name, String abbreviation,
                                   String type, int episodeLength, String date) throws SQLException {
        String sql =
            "INSERT INTO shows (name, abbreviation, type, avg_episode_length_mins, date_entered, last_updated) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, abbreviation);
            ps.setString(3, type);
            ps.setInt(4, episodeLength);
            ps.setString(5, date);
            ps.setString(6, date);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException("Failed to retrieve generated show ID.");
    }

    private static void insertSeason(Connection conn, int showId,
                                      String label, int episodes) throws SQLException {
        String sql = "INSERT INTO seasons (show_id, season_label, episodes, watched) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setString(2, label);
            ps.setInt(3, episodes);
            ps.executeUpdate();
        }
    }

    private static String generateAbbreviation(String name) {
        StringBuilder abbr = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (!word.isEmpty())
                abbr.append(Character.toUpperCase(word.charAt(0)));
        }
        return abbr.toString();
    }

    private static int getNonNegativeInt(Scanner scan) {
        int value = -1;
        while (value < 0) {
            try {
                value = Integer.parseInt(scan.nextLine().trim());
                if (value < 0) System.out.println("Please enter a non-negative number:");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number:");
            }
        }
        return value;
    }
}
