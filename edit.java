import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class edit {

    static final String DB_URL = "jdbc:sqlite:shows.sql";
    static final double FUZZY_THRESHOLD = 0.70;
    static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        System.out.println("Enter show/movie name or abbreviation:");
        String query = scan.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("No input provided.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            List<int[]> matches = findMatches(conn, query);

            if (matches.isEmpty()) {
                System.out.println("No matches found for: \"" + query + "\"");
                return;
            }

            int showId;

            if (matches.size() == 1) {
                showId = matches.get(0)[0];
            } else {
                System.out.println("\nMultiple matches found:");
                for (int i = 0; i < matches.size(); i++) {
                    String[] info = getShowSummary(conn, matches.get(i)[0]);
                    System.out.println("  [" + (i + 1) + "] " + info[0] + " (" + info[1] + ") - " + info[2]);
                }
                System.out.print("Pick a number: ");
                int choice = getIntInRange(1, matches.size());
                showId = matches.get(choice - 1)[0];
            }

            printShow(conn, showId);
            showEditMenu(conn, showId);
        }

        scan.close();
    }

    // ─── Edit Menu ────────────────────────────────────────────────────────────

    private static void showEditMenu(Connection conn, int showId) throws SQLException {
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("  [1] Edit name");
            System.out.println("  [2] Edit type (movie/show)");
            System.out.println("  [3] Edit episode length");
            System.out.println("  [4] Edit seasons/episodes");
            System.out.println("  [5] Add a season");
            System.out.println("  [6] Delete a season");
            System.out.println("  [7] Delete this entry entirely");
            System.out.println("  [0] Exit");
            System.out.print("Choice: ");

            int choice = getIntInRange(0, 7);

            switch (choice) {
                case 1 -> editName(conn, showId);
                case 2 -> editType(conn, showId);
                case 3 -> editEpisodeLength(conn, showId);
                case 4 -> editSeasonEpisodes(conn, showId);
                case 5 -> addSeason(conn, showId);
                case 6 -> deleteSeason(conn, showId);
                case 7 -> {
                    if (deleteShow(conn, showId)) return;
                }
                case 0 -> { return; }
            }
        }
    }

    // ─── Edit Actions ─────────────────────────────────────────────────────────

    private static void editName(Connection conn, int showId) throws SQLException {
        System.out.print("New name: ");
        String newName = scan.nextLine().trim();
        if (newName.isEmpty()) { System.out.println("Cancelled."); return; }

        String newAbbr = generateAbbreviation(newName);
        String today = LocalDate.now().toString();

        String sql = "UPDATE shows SET name = ?, abbreviation = ?, last_updated = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newAbbr);
            ps.setString(3, today);
            ps.setInt(4, showId);
            ps.executeUpdate();
        }
        System.out.println("Name updated to \"" + newName + "\" (abbr: " + newAbbr + ")");
    }

    private static void editType(Connection conn, int showId) throws SQLException {
        String newType = "";
        while (!newType.equals("movie") && !newType.equals("show")) {
            System.out.print("New type (movie/show): ");
            newType = scan.nextLine().toLowerCase().trim();
        }
        updateField(conn, showId, "type", newType);
        System.out.println("Type updated to \"" + newType + "\"");
    }

    private static void editEpisodeLength(Connection conn, int showId) throws SQLException {
        System.out.print("New average episode length (minutes): ");
        int len = getNonNegativeInt();
        updateField(conn, showId, "avg_episode_length_mins", String.valueOf(len));
        System.out.println("Episode length updated to " + len + " min.");
    }

    private static void editSeasonEpisodes(Connection conn, int showId) throws SQLException {
        List<int[]> seasons = getSeasons(conn, showId);
        if (seasons.isEmpty()) { System.out.println("No seasons found."); return; }

        System.out.println("\nSeasons:");
        for (int i = 0; i < seasons.size(); i++) {
            int[] s = seasons.get(i);
            System.out.println("  [" + (i + 1) + "] Season ID " + s[0] + " | " + getSeasonLabel(conn, s[0]) + " | " + s[1] + " episodes");
        }
        System.out.print("Pick season to edit: ");
        int pick = getIntInRange(1, seasons.size());
        int seasonId = seasons.get(pick - 1)[0];

        System.out.print("New episode count: ");
        int newEp = getNonNegativeInt();

        String sql = "UPDATE seasons SET episodes = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newEp);
            ps.setInt(2, seasonId);
            ps.executeUpdate();
        }
        updateLastUpdated(conn, showId);
        System.out.println("Episode count updated to " + newEp + ".");
    }

    private static void toggleWatched(Connection conn, int showId) throws SQLException {
        List<int[]> seasons = getSeasons(conn, showId);
        if (seasons.isEmpty()) { System.out.println("No seasons found."); return; }

        System.out.println("\nSeasons:");
        for (int i = 0; i < seasons.size(); i++) {
            int[] s = seasons.get(i);
            String label = getSeasonLabel(conn, s[0]);
            System.out.println("  [" + (i + 1) + "] " + label + " - Watched: " + (s[2] == 1 ? "Yes" : "No"));
        }
        System.out.print("Pick season to toggle: ");
        int pick = getIntInRange(1, seasons.size());
        int[] chosen = seasons.get(pick - 1);
        int newWatched = chosen[2] == 1 ? 0 : 1;

        String sql = "UPDATE seasons SET watched = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newWatched);
            ps.setInt(2, chosen[0]);
            ps.executeUpdate();
        }
        updateLastUpdated(conn, showId);
        System.out.println("Marked as " + (newWatched == 1 ? "Watched" : "Unwatched") + ".");
    }

    private static void addSeason(Connection conn, int showId) throws SQLException {
        System.out.print("Season label (e.g. Season 4): ");
        String label = scan.nextLine().trim();
        if (label.isEmpty()) { System.out.println("Cancelled."); return; }

        System.out.print("Number of episodes: ");
        int eps = getNonNegativeInt();

        String sql = "INSERT INTO seasons (show_id, season_label, episodes, watched) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ps.setString(2, label);
            ps.setInt(3, eps);
            ps.executeUpdate();
        }
        updateLastUpdated(conn, showId);
        System.out.println("Season \"" + label + "\" added.");
    }

    private static void deleteSeason(Connection conn, int showId) throws SQLException {
        List<int[]> seasons = getSeasons(conn, showId);
        if (seasons.isEmpty()) { System.out.println("No seasons to delete."); return; }

        System.out.println("\nSeasons:");
        for (int i = 0; i < seasons.size(); i++) {
            int[] s = seasons.get(i);
            System.out.println("  [" + (i + 1) + "] " + getSeasonLabel(conn, s[0]) + " | " + s[1] + " episodes");
        }
        System.out.print("Pick season to delete (0 to cancel): ");
        int pick = getIntInRange(0, seasons.size());
        if (pick == 0) { System.out.println("Cancelled."); return; }

        int seasonId = seasons.get(pick - 1)[0];
        String sql = "DELETE FROM seasons WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seasonId);
            ps.executeUpdate();
        }
        updateLastUpdated(conn, showId);
        System.out.println("Season deleted.");
    }

    private static boolean deleteShow(Connection conn, int showId) throws SQLException {
        String[] info = getShowSummary(conn, showId);
        System.out.print("Are you sure you want to delete \"" + info[0] + "\"? (yes/no): ");
        String confirm = scan.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) { System.out.println("Cancelled."); return false; }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM seasons WHERE show_id = ?")) {
            ps.setInt(1, showId); ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM shows WHERE id = ?")) {
            ps.setInt(1, showId); ps.executeUpdate();
        }
        System.out.println("\"" + info[0] + "\" deleted.");
        return true;
    }

    // ─── Display ──────────────────────────────────────────────────────────────

    private static void printShow(Connection conn, int showId) throws SQLException {
        String sql = "SELECT * FROM shows WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;

            System.out.println("\n========================================");
            System.out.println("Name         : " + rs.getString("name"));
            System.out.println("Abbreviation : " + rs.getString("abbreviation"));
            System.out.println("Type         : " + rs.getString("type"));
            System.out.println("Episode Len  : " + rs.getInt("avg_episode_length_mins") + " min");
            System.out.println("Date Added   : " + rs.getString("date_entered"));
            System.out.println("Last Updated : " + rs.getString("last_updated"));
        }

        String seasonSql = "SELECT season_label, episodes, watched FROM seasons WHERE show_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(seasonSql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nSeasons:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("  %-14s | %d ep | Watched: %s%n",
                    rs.getString("season_label"),
                    rs.getInt("episodes"),
                    rs.getInt("watched") == 1 ? "Yes" : "No");
            }
            if (!any) System.out.println("  No seasons found.");
            System.out.println("========================================");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static void updateField(Connection conn, int showId, String column, String value) throws SQLException {
        String sql = "UPDATE shows SET " + column + " = ?, last_updated = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, showId);
            ps.executeUpdate();
        }
    }

    private static void updateLastUpdated(Connection conn, int showId) throws SQLException {
        String sql = "UPDATE shows SET last_updated = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.setInt(2, showId);
            ps.executeUpdate();
        }
    }

    private static List<int[]> getSeasons(Connection conn, int showId) throws SQLException {
        List<int[]> list = new ArrayList<>();
        String sql = "SELECT id, episodes, watched FROM seasons WHERE show_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new int[]{rs.getInt("id"), rs.getInt("episodes"), rs.getInt("watched")});
        }
        return list;
    }

    private static String getSeasonLabel(Connection conn, int seasonId) throws SQLException {
        String sql = "SELECT season_label FROM seasons WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seasonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("season_label");
        }
        return "Unknown";
    }

    private static String[] getShowSummary(Connection conn, int showId) throws SQLException {
        String sql = "SELECT name, abbreviation, type FROM shows WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new String[]{rs.getString("name"), rs.getString("abbreviation"), rs.getString("type")};
        }
        return new String[]{"Unknown", "", ""};
    }

    private static List<int[]> findMatches(Connection conn, String query) throws SQLException {
        List<int[]> ids = new ArrayList<>();
        String queryLower = query.toLowerCase();
        String sql = "SELECT id, name, abbreviation FROM shows";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name").toLowerCase();
                String abbr = rs.getString("abbreviation");
                if (abbr == null) abbr = "";
                abbr = abbr.toLowerCase();

                if (name.equals(queryLower) || abbr.equals(queryLower)) {
                    ids.add(new int[]{id}); continue;
                }
                if (similarity(queryLower, name) >= FUZZY_THRESHOLD ||
                    (!abbr.isEmpty() && similarity(queryLower, abbr) >= FUZZY_THRESHOLD)) {
                    ids.add(new int[]{id});
                }
            }
        }
        return ids;
    }

    private static String generateAbbreviation(String name) {
        StringBuilder abbr = new StringBuilder();
        for (String word : name.trim().split("\\s+"))
            if (!word.isEmpty()) abbr.append(Character.toUpperCase(word.charAt(0)));
        return abbr.toString();
    }

    private static double similarity(String a, String b) {
        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int maxLen = Math.max(a.length(), b.length());
        return 1.0 - ((double) levenshtein(a, b) / maxLen);
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(prev[j] + 1, curr[j - 1] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev; prev = curr; curr = temp;
        }
        return prev[b.length()];
    }

    private static int getNonNegativeInt() {
        int value = -1;
        while (value < 0) {
            try {
                value = Integer.parseInt(scan.nextLine().trim());
                if (value < 0) System.out.println("Please enter a non-negative number:");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number:");
            }
        }
        return value;
    }

    private static int getIntInRange(int min, int max) {
        int value = -1;
        while (value < min || value > max) {
            try {
                value = Integer.parseInt(scan.nextLine().trim());
                if (value < min || value > max)
                    System.out.println("Enter a number between " + min + " and " + max + ":");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number:");
            }
        }
        return value;
    }
}