public class Movie {
    public String title;
    public int year = 0;
    public String director = "null";
    public int runtime;
    public int simkl_id = 0;

    
    public Movie(String title, int year, String director, int runtime, int simkl_id) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.runtime = runtime;
        this.simkl_id = simkl_id;
    }

    public Movie(String title, int runtime) {
        this.title = title;
        this.runtime = runtime;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== MOVIE INFO =====\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Year: ").append(year).append("\n");
        sb.append("Director: ").append(director).append("\n");
        sb.append("Runtime: ").append(runtime).append(" minutes\n");
        return sb.toString();
    }
}