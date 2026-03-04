import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class list {
    public static void main(String[] args) throws IOException {
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
            // Just ask how many seasons directly
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

        File file = new File("shows.csv");

        if (!file.exists()) {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.println("Show Name,Abbreviation,Type,Avg Episode Length (mins),Date Entered,Last Updated,Season,Episodes,Watched");
            pw.close();
        }

        PrintWriter pw = new PrintWriter(new FileWriter(file, true));

        String firstSeason = type.equals("movie") ? "Movie" : "Season 1";
        pw.println(
            "\"" + name + "\"," +
            abbreviation + "," +
            type + "," +
            episodeLength + "," +
            dateEntered + "," +
            dateEntered + "," +
            firstSeason + "," +
            seasonEpisodes.get(0) + "," +
            "0"
        );

        for (int i = 1; i < seasonEpisodes.size(); i++) {
            pw.println(
                ",,,,,," +
                "Season " + (i + 1) + "," +
                seasonEpisodes.get(i) + "," +
                "0"
            );
        }

        pw.println(",,,,,,,,,");
        pw.close();

        System.out.println("\n\"" + name + "\" saved to shows.csv!");
        scan.close();
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