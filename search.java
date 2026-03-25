import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class search {

    static final String DB_URL = "jdbc:sqlite:shows.sql";
    static final double FUZZY_THRESHOLD = 0.70;

    public static void main(String[] args) throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter show/movie name or abbreviation to search:");
        String query = scan.nextLine().trim();
        scan.close();

        if (query.isEmpty()) {
            System.out.println("No input provided.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            List<Integer> matchedIds = findMatches(conn, query);

            if (matchedIds.isEmpty()) {
                System.out.println("No matches found for: \"" + query + "\"");
                return;
            }

            for (int showId : matchedIds) {
                displayShow(conn, showId);
                System.out.println("----------------------------------------");
            }
        }
    }

    // Returns list of show IDs that match the query
    private static List<Integer> findMatches(Connection conn, String query) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String queryLower = query.toLowerCase();

        String sql = "SELECT id, name, abbreviation FROM shows";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name").toLowerCase();
                String abbr = rs.getString("abbreviation");
                if (abbr == null) abbr = "";
                abbr = abbr.toLowerCase();

                // 1. Exact name match
                if (name.equals(queryLower)) {
                    ids.add(id);
                    continue;
                }

                // 2. Abbreviation match (case-insensitive)
                if (!abbr.isEmpty() && abbr.equals(queryLower)) {
                    ids.add(id);
                    continue;
                }

                // 3. Fuzzy match on name (>= 70% similarity)
                double sim = similarity(queryLower, name);
                if (sim >= FUZZY_THRESHOLD) {
                    ids.add(id);
                    continue;
                }

                // 4. Fuzzy match on abbreviation
                double abbrSim = similarity(queryLower, abbr);
                if (!abbr.isEmpty() && abbrSim >= FUZZY_THRESHOLD) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    // Displays full info for a show
    private static void displayShow(Connection conn, int showId) throws SQLException {
        String showSql = "SELECT * FROM shows WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(showSql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;

            String name     = rs.getString("name");
            String abbr     = rs.getString("abbreviation");
            String type     = rs.getString("type");
            int    epLen    = rs.getInt("avg_episode_length_mins");
            String entered  = rs.getString("date_entered");
            String updated  = rs.getString("last_updated");

            System.out.println("\nName         : " + name);
            System.out.println("Abbreviation : " + abbr);
            System.out.println("Type         : " + type);
            System.out.println("Episode Len  : " + epLen + " min");
            System.out.println("Date Added   : " + entered);
            System.out.println("Last Updated : " + updated);
        }

        // Display seasons
        String seasonSql = "SELECT season_label, episodes, watched FROM seasons WHERE show_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(seasonSql)) {
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nSeasons:");
            boolean hasSeasons = false;
            while (rs.next()) {
                hasSeasons = true;
                String label    = rs.getString("season_label");
                int    episodes = rs.getInt("episodes");
                int    watched  = rs.getInt("watched");
                System.out.printf("  %-12s | %d episode(s) | Watched: %s%n",
                    label, episodes, watched == 1 ? "Yes" : "No");
            }
            if (!hasSeasons) System.out.println("  No season data found.");
        }
    }

    private static double similarity(String a, String b) {
        if (a.equals(b)) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        int maxLen = Math.max(a.length(), b.length());
        int dist   = levenshtein(a, b);
        return 1.0 - ((double) dist / maxLen);
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) prev[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                    Math.min(prev[j] + 1, curr[j - 1] + 1),
                    prev[j - 1] + cost
                );
            }
            int[] temp = prev; prev = curr; curr = temp;
        }
        return prev[b.length()];
    }
}
