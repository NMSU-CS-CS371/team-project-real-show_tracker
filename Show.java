public class Show {
    public String title;
    public int year;
    public int seasons;
    public int [] episodesPerSeason;
    public int simkl_id;

    //TO-DO IMPLEMENT int[] FOR EPISODE COUNT BETWEEN SEASONS

    public Show(String title, int year, int seasons, int [] episodesPerSeason, int simkl_id) {
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
        sb.append("Title: ").append(title).append("\n");
        sb.append("Year: ").append(year).append("\n");
        sb.append("Seasons: ").append(seasons).append("\n");
        sb.append("Episodes: ");
        if (episodesPerSeason != null) {
            for(int i = 1; i < episodesPerSeason.length + 1; i++){
                sb.append("[Season " + i + ": " + episodesPerSeason[i-1] + "], ");
            }
        } else {
            sb.append("No episode info available");
        }
        sb.append("\n");
        
        return sb.toString();
    }
}