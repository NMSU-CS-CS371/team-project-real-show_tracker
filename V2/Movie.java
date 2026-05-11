public class Movie {
    public String title;
    public int year;
    public String director;
    public int runtime;
    public int simkl_id;
    public String poster_url;

    public Movie(String title, int year, String director, int runtime, int simkl_id) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.runtime = runtime;
        this.simkl_id = simkl_id;
        this.poster_url = "";
    }

    public Movie(String title, int year, String director, int runtime, int simkl_id, String poster_url) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.runtime = runtime;
        this.simkl_id = simkl_id;
        this.poster_url = poster_url;
    }

    @Override
    public String toString() {
        return "Movie{" +
               "title='" + title + '\'' +
               ", year=" + year +
               ", director='" + director + '\'' +
               ", runtime=" + runtime +
               ", simkl_id=" + simkl_id +
               ", poster_url='" + poster_url + '\'' +
               '}';
    }
}
