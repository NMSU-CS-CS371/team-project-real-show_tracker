let currentSearchResults = [];
let myWatchlist = [];

document.addEventListener('DOMContentLoaded', function() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');
    const resultsContainer = document.getElementById('resultsContainer');

    searchBtn.addEventListener('click', function() {
        const query = searchInput.value.trim();
        if (query) {
            searchMovies(query);
        }
    });

    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const query = searchInput.value.trim();
            if (query) {
                searchMovies(query);
            }
        }
    });
});

async function searchMovies(query) {
    const resultsContainer = document.getElementById('resultsContainer');
    resultsContainer.innerHTML = '<p class="placeholder-text">Searching...</p>';

    try {
        const response = await fetch(`http://localhost:8080/search?query=${encodeURIComponent(query)}`);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const results = await response.json();

        if (results.message) {
            resultsContainer.innerHTML = `<p class="placeholder-text">${results.message}</p>`;
            return;
        }

        currentSearchResults = results;
        displayResults(results);
    } catch (error) {
        console.error('Error fetching movies:', error);
        resultsContainer.innerHTML = '<p class="placeholder-text">Error searching movies. Please try again.</p>';
    }
}

function displayResults(results) {
    const resultsContainer = document.getElementById('resultsContainer');
    resultsContainer.innerHTML = '';

    if (results.length === 0) {
        resultsContainer.innerHTML = '<p class="placeholder-text">No movies found.</p>';
        return;
    }

    results.forEach(movie => {
        const movieDiv = document.createElement('div');
        movieDiv.className = 'movie-item glass-panel';
        movieDiv.innerHTML = `
            <h3>${movie.title || 'Unknown Title'}</h3>
            <p>Year: ${movie.year || 'N/A'}</p>
            <p>Director: ${movie.director || 'N/A'}</p>
            <p>ID: ${movie.ids ? movie.ids.simkl_id : 'N/A'}</p>
            <button class="add-btn" onclick="addToWatchlist(${JSON.stringify(movie).replace(/"/g, '&quot;')})">Add to Watchlist</button>
        `;
        resultsContainer.appendChild(movieDiv);
    });
}

function addToWatchlist(movie) {
    myWatchlist.push(movie);
    updateWatchlistDisplay();
}

function updateWatchlistDisplay() {
    const watchlistContainer = document.getElementById('watchlistContainer');
    watchlistContainer.innerHTML = '';

    if (myWatchlist.length === 0) {
        watchlistContainer.innerHTML = '<p class="placeholder-text">Your roster is empty.</p>';
        return;
    }

    myWatchlist.forEach(movie => {
        const movieDiv = document.createElement('div');
        movieDiv.className = 'watchlist-item glass-panel';
        movieDiv.innerHTML = `
            <h3>${movie.title || 'Unknown Title'}</h3>
            <p>Year: ${movie.year || 'N/A'}</p>
            <button class="remove-btn" onclick="removeFromWatchlist(${myWatchlist.indexOf(movie)})">Remove</button>
        `;
        watchlistContainer.appendChild(movieDiv);
    });
}

function removeFromWatchlist(index) {
    myWatchlist.splice(index, 1);
    updateWatchlistDisplay();
}