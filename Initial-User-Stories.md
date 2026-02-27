# Functional Requirements: User Stories

| ID | Title | User Story Statement | Effort (Fibonacci) |
| :--- | :--- | :--- | :--- |
| **1.0** | **Simple Search** | As a user, I want to search a database of shows so that I can quickly add them to my personal lists without typing every detail manually. | 5 |
| **2.0** | **Episode Tracker** | As a binge-watcher, I want to click a button to increment my "Current Episode" so that I always know exactly where to resume my show. | 3 |
| **3.0** | **The Backlog** | As an organized fan, I want to save shows to a "Plan to Watch" list so that I don't forget recommendations from friends. | 3 |
| **4.0** | **Habit Metrics** | As a data-conscious user, I want to see a summary of my total time watched and top genres so that I can analyze my viewing patterns. | 8 |
| **5.0** | **Show Ratings** | As a critic, I want to give each show a 1-10 rating so that I can remember which series I actually enjoyed and which were a waste of time. | 2 |
| **6.0** | **Export History** | As a user, I want to export my list to a simple text file so that I have a permanent backup of my data that I can keep forever. | 5 |

---

## Detailed Elaboration & Acceptance Tests

### 1.0: Simple Search
* **Elaboration:** Users need a way to find shows. To keep it simple, we will use a basic search that returns Title, Genre, and Total Episodes from a simple API (like TMDB).
* **Acceptance Test:** Type "One Piece" into the search bar. Results should display the show title and a "Add to List" button.

### 4.0: Habit Metrics (The Metric Tracker)
* **Elaboration:** This is the core "Habit Analysis" feature. The app will look at all "Completed" shows, multiply the number of episodes by an average duration (e.g., 24 mins for anime), and show a breakdown of genres.
* **Acceptance Test:** If a user logs 10 episodes of an "Action" show, the Statistics page should show "240 Minutes Watched" and "100% Action Genre."

### 6.0: Export History
* **Elaboration:** To avoid complex cloud database hosting, this allows users to save their data locally as a CSV or Text file.
* **Acceptance Test:** Click the "Export" button. A file named `my_shows.csv` should download containing the names and ratings of all tracked shows.