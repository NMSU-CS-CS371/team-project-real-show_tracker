import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("\n=============================");
            System.out.println("     SHOW/MOVIE TRACKER      ");
            System.out.println("=============================");
            System.out.println("1. Add a show/movie manually");
            System.out.println("2. Add a show/movie automatically");
            System.out.println("3. Search for a show/movie");
            System.out.println("4. Edit a show/movie");
            System.out.println("0. Exit");
            System.out.print("\nCHOICE: ");

            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n--- Manual Entry ---");
                    manualShow.main(new String[]{});
                    break;
                case 2:
                    System.out.println("\n--- Auto Entry ---");

                    // ── Type ──────────────────────────────────────────────
                    String type = "";
                    while (!type.equals("movie") && !type.equals("show")) {
                        System.out.print("Is this a movie or a show? (movie/show): ");
                        type = scan.nextLine().toLowerCase().trim();
                        System.out.println();
                        if (!type.equals("movie") && !type.equals("show"))
                            System.out.println("Please type exactly 'movie' or 'show'.");
                    }
                    System.out.print("Enter " + Character.toUpperCase(type.charAt(0)) + type.substring(1) + " Name: ");
                    String query = scan.nextLine();
                    System.out.println();

                    if (type.equals("movie")) {
                        Movie toAdd = autoShow.searchMovie(query);
                        if (toAdd != null) {
                            list.saveEntry(toAdd);
                        } else {
                            System.out.println("ERROR: No movie selected or search failed; nothing stored.");
                        }
                    } else {
                        Show toAdd = autoShow.searchShow(query);
                        if (toAdd != null) {
                            list.saveEntry(toAdd);
                        } else {
                            System.out.println("ERROR: No show selected or search failed; nothing stored.");
                        }
                    }
                    System.out.println(Character.toUpperCase(type.charAt(0)) + type.substring(1) + " Stored Correctly!");
                    break;
                case 3:
                    System.out.println("\n--- Search ---");
                    search.main(new String[]{});
                    break;
                case 4:
                    System.out.println("\n--- Edit ---");
                    edit.main(new String[]{});
                    break;
                case 0:
                    System.out.println("\nGoodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 0-4.");
            }
        }

        scan.close();
    }
}
