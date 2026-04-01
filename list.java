import java.sql.*;
import java.time.LocalDate;

public class list {

    static final String DB_URL = "jdbc:sqlite:shows.sql";

    // ── Accept a Movie object ─────────────────────────────────────────
    public static void saveEntry(Movie movie) throws SQLException {
        String date = LocalDate.now().toString();
        String abbreviation = generateAbbreviation(movie.title);
        System.out.println("Abbreviation generated: " + abbreviation);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTablesIfNeeded(conn);
            int showId = insertShow(conn, movie.title, abbreviation, "movie",
                                    movie.runtime, movie.year, movie.director,
                                    movie.simkl_id, date);
            insertSeason(conn, showId, "Movie", 1);
        }
    }

    // ── Accept a Show object ──────────────────────────────────────────
    public static void saveEntry(Show show) throws SQLException {
        String date = LocalDate.now().toString();
        String abbreviation = generateAbbreviation(show.title);
        System.out.println("Abbreviation generated: " + abbreviation);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTablesIfNeeded(conn);
            int showId = insertShow(conn, show.title, abbreviation, "show",
                                    show.avgEpisodeLength, show.year, "N/A",
                                    show.simkl_id, date);

            for (int i = 0; i < show.episodesPerSeason.length; i++) {
                insertSeason(conn, showId, "Season " + (i + 1), show.episodesPerSeason[i]);
            }
        }
    }

    // ── DB helpers ────────────────────────────────────────────────────
    static void createTablesIfNeeded(Connection conn) throws SQLException {
        String createShows =
            "CREATE TABLE IF NOT EXISTS shows (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL," +
            "  abbreviation TEXT," +
            "  type TEXT," +
            "  avg_episode_length_mins INTEGER," +
            "  year INTEGER," +
            "  director TEXT," +
            "  simkl_id INTEGER," +
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

        ensureColumnExists(conn, "shows", "year", "INTEGER");
        ensureColumnExists(conn, "shows", "avg_episode_length_mins", "INTEGER");
        ensureColumnExists(conn, "shows", "director", "TEXT");
        ensureColumnExists(conn, "shows", "simkl_id", "INTEGER");
        ensureColumnExists(conn, "shows", "date_entered", "TEXT");
        ensureColumnExists(conn, "shows", "last_updated", "TEXT");
    }

    static void ensureColumnExists(Connection conn, String table, String column, String type) throws SQLException {
        String pragma = "PRAGMA table_info(" + table + ")";
        boolean exists = false;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pragma)) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + type;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    static int insertShow(Connection conn, String name, String abbreviation,
                           String type, int episodeLength, int year,
                           String director, int simklId, String date) throws SQLException {
        String sql =
            "INSERT INTO shows (name, abbreviation, type, avg_episode_length_mins, year, director, simkl_id, date_entered, last_updated) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, abbreviation);
            ps.setString(3, type);
            ps.setInt(4, episodeLength);
            ps.setInt(5, year);
            ps.setString(6, director);
            ps.setInt(7, simklId);
            ps.setString(8, date);
            ps.setString(9, date);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException("Failed to retrieve generated show ID.");
    }

    static void insertSeason(Connection conn, int showId,
                              String label, int episodes) throws SQLException {
        String sql = "INSERT INTO seasons (show_id, season_label, episodes, watched) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setString(2, label);
            ps.setInt(3, episodes);
            ps.executeUpdate();
        }
    }

    static String generateAbbreviation(String name) {
        StringBuilder abbr = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (!word.isEmpty())
                abbr.append(Character.toUpperCase(word.charAt(0)));
        }
        return abbr.toString();
    }
}
