public class Show {
    public String title;
    public int year;
    public int seasons;
    public int[] episodesPerSeason;
    public int simkl_id;
    public int avgEpisodeLength; // added for manualShow / list compatibility

    public Show(String title, int year, int seasons, int[] episodesPerSeason, int simkl_id) {
        this.title = title;
        this.year = year;
        this.seasons = seasons;
        this.episodesPerSeason = episodesPerSeason;
        this.simkl_id = simkl_id;
    }

    public Show(String title, int seasons) {
        this.title = title;
        this.seasons = seasons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== SHOW INFO =====\n");
        sb.append("Title   : ").append(title).append("\n");
        sb.append("Year    : ").append(year).append("\n");
        sb.append("Seasons : ").append(seasons).append("\n");
        sb.append("Avg Ep  : ").append(avgEpisodeLength).append(" min\n");
        sb.append("Episodes: ");
        if (episodesPerSeason != null) {
            for (int i = 0; i < episodesPerSeason.length; i++) {
                sb.append("[Season ").append(i + 1).append(": ").append(episodesPerSeason[i]).append("] ");
            }
        } else {
            sb.append("No episode info available");
        }
        sb.append("\n");
        return sb.toString();
    }
}
