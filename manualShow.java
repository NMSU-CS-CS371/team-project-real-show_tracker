import java.util.Scanner;

public class manualShow {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);

        // ── Name ──────────────────────────────────────────────
        System.out.println("Enter the name of the show/movie:");
        String name = scan.nextLine().trim();

        // ── Type ──────────────────────────────────────────────
        String type = "";
        while (!type.equals("movie") && !type.equals("show")) {
            System.out.println("Is this a movie or a show? (movie/show)");
            type = scan.nextLine().toLowerCase().trim();
            if (!type.equals("movie") && !type.equals("show"))
                System.out.println("Please type exactly 'movie' or 'show'.");
        }

        if (type.equals("movie")) {
            // ── Movie ─────────────────────────────────────────
            System.out.println("What year was it released?");
            int year = getNonNegativeInt(scan);

            System.out.println("Who directed it? (press Enter to skip)");
            String director = scan.nextLine().trim();
            if (director.isEmpty()) director = "Unknown";

            System.out.println("How long is the movie? (in minutes)");
            int runtime = getNonNegativeInt(scan);

            Movie movie = new Movie(name, year, director, runtime, -1);
            System.out.println(movie);
            list.saveEntry(movie);

        } else {
            // ── Show ──────────────────────────────────────────
            System.out.println("What year did it first air?");
            int year = getNonNegativeInt(scan);

            System.out.println("How many seasons are there?");
            int numSeasons = getNonNegativeInt(scan);

            int[] episodesPerSeason = new int[numSeasons];

            if (numSeasons > 1) {
                String sameEps = "";
                while (!sameEps.equals("yes") && !sameEps.equals("no")) {
                    System.out.println("Does every season have the same number of episodes? (yes/no)");
                    sameEps = scan.nextLine().toLowerCase().trim();
                    if (!sameEps.equals("yes") && !sameEps.equals("no"))
                        System.out.println("Please type exactly 'yes' or 'no'.");
                }

                if (sameEps.equals("yes")) {
                    System.out.println("How many episodes per season?");
                    int eps = getNonNegativeInt(scan);
                    for (int i = 0; i < numSeasons; i++)
                        episodesPerSeason[i] = eps;
                } else {
                    for (int i = 0; i < numSeasons; i++) {
                        System.out.println("How many episodes in Season " + (i + 1) + "?");
                        episodesPerSeason[i] = getNonNegativeInt(scan);
                    }
                }
            } else {
                System.out.println("How many episodes in Season 1?");
                episodesPerSeason[0] = getNonNegativeInt(scan);
            }

            // ── Total runtime → avg per episode ───────────────
            int totalEpisodes = 0;
            for (int eps : episodesPerSeason) totalEpisodes += eps;

            System.out.println("What is the total runtime of the show? (in minutes)");
            System.out.println("(Total across all " + totalEpisodes + " episodes)");
            int totalRuntime = getNonNegativeInt(scan);

            int avgEpisodeLength = (totalEpisodes > 0) ? (totalRuntime / totalEpisodes) : 0;
            System.out.println("Average episode length: " + avgEpisodeLength + " min (" 
                + totalRuntime + " min ÷ " + totalEpisodes + " episodes)");

            Show show = new Show(name, year, numSeasons, episodesPerSeason, -1);
            show.avgEpisodeLength = avgEpisodeLength;
            System.out.println(show);
            list.saveEntry(show);
        }

        System.out.println("\n\"" + name + "\" saved successfully!");
        scan.close();
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
