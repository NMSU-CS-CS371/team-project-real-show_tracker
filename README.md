# Show Tracker
# Project by Scottie, Matt, and Josue

A Java-backed web app used for searching and saving movies to a watchlist while tracking daily viewing habits.
(README formatting suggestions were made by chatGPT. Query: "Give me an example README file that I can edit with my project's information")

## Project Overview

This project contains a small frontend and a Java backend server.

- `index2.html` - main browser UI for search, manual entry, watchlist, and calendar.
- `style.css` - page styling for the HTML.
- `app2.js` - Our frontend logic, fetch calls, watchlist rendering, and calendar implementation.
- `webServ.java` - Java HTTP server that serves files to the frontend and provides API functionalility.
- `autoShow.java` - communicates with the Simkl API to search movies and fetch movie details.
- `list.java` - saves and loads watchlist entries in a local SQLite database.
- `Movie.java` / `Show.java` - classes used by the backend.
- `shows.sql` - SQLite database file used to store/load saved watchlist entries on the local server machine (can be copied/shared to other machines).
- `lib/` - folder with required runtime libraries for JSON handling and SQLite JDBC.

## Repo Structure

- `index2.html`
- `style.css`
- `app2.js`
- `webServ.java`
- `autoShow.java`
- `list.java`
- `Movie.java`
- `Show.java`
- `shows.sql`
- `lib/json-20251224.jar`
- `lib/sqlite-jdbc-3.51.2.0.jar`

artifacts from the build and non-essential files:

- `*.class` files
- `.DS_Store`
- `webServ$*.class`

(generated outputs or OS metadata and are not necessecary)

## Dependencies

This project requires:

- Java 11 or newer (`javac` and `java`)
- Internet access for the Simkl API search functionality
- SQLite runtime via JDBC

Included library jar files:

- `lib/json-20251224.jar`
- `lib/sqlite-jdbc-3.51.2.0.jar`

## Build Instructions

Open a terminal in the project root and run:

```bash
javac -cp "lib/json-20251224.jar:lib/sqlite-jdbc-3.51.2.0.jar" \
    webServ.java autoShow.java list.java Movie.java Show.java
```

If you want to compile all Java source files in the directory, you can also use:

```bash
javac -cp "lib/json-20251224.jar:lib/sqlite-jdbc-3.51.2.0.jar" *.java
```

## Run Instructions

Start the backend server from the project root:

```bash
java -cp ".:lib/json-20251224.jar:lib/sqlite-jdbc-3.51.2.0.jar" webServ
```

Then open your browser to:

```text
http://localhost:8080
```

Then the frontend will load and communicate with the backend via the following slugs:

- `GET /search?query=...` — search movies through Simkl
- `GET /watchlist/load` — load saved watchlist entries
- `POST /watchlist/save` — save a movie to the watchlist
- `DELETE /watchlist/remove?simkl_id=...` — remove a saved entry

## How to Use the App

1. Open `http://localhost:8080` in your browser.
2. Search for a movie using the search bar.
3. Click `Add to Watchlist` to save a searched title.
4. Use `+ Manual Entry` to add a custom movie without search results.
5. The right column shows your active roster and a monthly calendar of watch time.

## Notes

- The Simkl API key is currently hard-coded inside `autoShow.java`.
- The local database file `shows.sql` is created and updated automatically when saving entries.
- Manual entries are currently stored locally in the browser session, while search results are persisted to SQLite.

## Troubleshooting

- If the browser cannot connect, verify the server is running on port `8080`. The terminal running the server should say "Server running on port 8080"
- If JSON or SQLite classes are missing, ensure the `lib/` jars are present and referenced in the classpath correctly.
