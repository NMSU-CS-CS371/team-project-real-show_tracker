import java.sql.*;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

public class list {

    static final String DB_URL = "jdbc:sqlite:shows.sql";

    // ── Accept a Movie object ─────────────────────────────────────────
    public static void saveEntry(Movie movie) throws SQLException {
        String date = LocalDate.now().toString();
        String abbreviation = generateAbbreviation(movie.title);
        System.out.println("Abbreviation generated: " + abbreviation);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTablesIfNeeded(conn);
            Integer existingShowId = getShowIdBySimklId(conn, movie.simkl_id);
            if (existingShowId != null && movie.simkl_id != 0) {
                System.out.println("Movie already in watchlist: " + movie.title);
                return;
            }

            int showId = insertShow(conn, movie.title, abbreviation, "movie",
                                    movie.runtime, movie.year, movie.director,
                                    movie.simkl_id, movie.poster_url, date);
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
            Integer existingShowId = getShowIdBySimklId(conn, show.simkl_id);
            if (existingShowId != null && show.simkl_id != 0) {
                System.out.println("Show already in watchlist: " + show.title);
                return;
            }

            int showId = insertShow(conn, show.title, abbreviation, "show",
                                    show.avgEpisodeLength, show.year, "N/A",
                                    show.simkl_id, "", date);

            for (int i = 0; i < show.episodesPerSeason.length; i++) {
                insertSeason(conn, showId, "Season " + (i + 1), show.episodesPerSeason[i]);
            }
        }
    }

    public static JSONArray loadEntries() throws SQLException {
        JSONArray entries = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTablesIfNeeded(conn);
            String sql = "SELECT id, name, abbreviation, type, avg_episode_length_mins, year, director, simkl_id, poster_url, date_entered, last_updated FROM shows ORDER BY date_entered DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    JSONObject entry = new JSONObject();
                    entry.put("id", rs.getInt("id"));
                    entry.put("title", rs.getString("name"));
                    entry.put("abbreviation", rs.getString("abbreviation"));
                    entry.put("type", rs.getString("type"));
                    entry.put("runtime", rs.getInt("avg_episode_length_mins"));
                    entry.put("year", rs.getInt("year"));
                    entry.put("director", rs.getString("director"));
                    entry.put("simkl_id", rs.getInt("simkl_id"));
                    entry.put("poster_url", rs.getString("poster_url"));
                    entry.put("date_entered", rs.getString("date_entered"));
                    entry.put("last_updated", rs.getString("last_updated"));

                    if ("show".equalsIgnoreCase(rs.getString("type"))) {
                        JSONArray seasons = new JSONArray();
                        int showId = rs.getInt("id");
                        String seasonsQuery = "SELECT season_label, episodes, watched FROM seasons WHERE show_id = ? ORDER BY id";
                        try (PreparedStatement seasonPs = conn.prepareStatement(seasonsQuery)) {
                            seasonPs.setInt(1, showId);
                            try (ResultSet seasonRs = seasonPs.executeQuery()) {
                                while (seasonRs.next()) {
                                    JSONObject seasonObj = new JSONObject();
                                    seasonObj.put("season_label", seasonRs.getString("season_label"));
                                    seasonObj.put("episodes", seasonRs.getInt("episodes"));
                                    seasonObj.put("watched", seasonRs.getInt("watched"));
                                    seasons.put(seasonObj);
                                }
                            }
                        }
                        entry.put("seasons", seasons);
                    }

                    entries.put(entry);
                }
            }
        }

        return entries;
    }

    public static boolean deleteEntry(int simklId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTablesIfNeeded(conn);
            Integer showId = getShowIdBySimklId(conn, simklId);
            if (showId == null) {
                return false;
            }

            try (PreparedStatement deleteSeasons = conn.prepareStatement("DELETE FROM seasons WHERE show_id = ?")) {
                deleteSeasons.setInt(1, showId);
                deleteSeasons.executeUpdate();
            }

            try (PreparedStatement deleteShow = conn.prepareStatement("DELETE FROM shows WHERE id = ?")) {
                deleteShow.setInt(1, showId);
                deleteShow.executeUpdate();
            }

            return true;
        }
    }

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
            "  poster_url TEXT," +
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
        ensureColumnExists(conn, "shows", "poster_url", "TEXT");
        ensureColumnExists(conn, "shows", "date_entered", "TEXT");
        ensureColumnExists(conn, "shows", "last_updated", "TEXT");
    }

    static Integer getShowIdBySimklId(Connection conn, int simklId) throws SQLException {
        String sql = "SELECT id FROM shows WHERE simkl_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, simklId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
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
                           String director, int simklId, String posterUrl, String date) throws SQLException {
        String sql =
            "INSERT INTO shows (name, abbreviation, type, avg_episode_length_mins, year, director, simkl_id, poster_url, date_entered, last_updated) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, abbreviation);
            ps.setString(3, type);
            ps.setInt(4, episodeLength);
            ps.setInt(5, year);
            ps.setString(6, director);
            ps.setInt(7, simklId);
            ps.setString(8, posterUrl);
            ps.setString(9, date);
            ps.setString(10, date);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
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
