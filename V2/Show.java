public class Show {
    public String title;
    public int year;
    public int avgEpisodeLength;
    public int seasons;
    public int[] episodesPerSeason;
    public int simkl_id;

    public Show(String title, int year, int avgEpisodeLength, int seasons, int[] episodesPerSeason, int simkl_id) {
        this.title = title;
        this.year = year;
        this.avgEpisodeLength = avgEpisodeLength;
        this.seasons = seasons;
        this.episodesPerSeason = episodesPerSeason;
        this.simkl_id = simkl_id;
    }

    public Show(String title, int year, int seasons, int[] episodesPerSeason, int simkl_id) {
        this(title, year, 0, seasons, episodesPerSeason, simkl_id);
    }

    @Override
    public String toString() {
        return "Show{" +
               "title='" + title + '\'' +
               ", year=" + year +
               ", avgEpisodeLength=" + avgEpisodeLength +
               ", seasons=" + seasons +
               ", simkl_id=" + simkl_id +
               '}';
    }
}
