let currentSearchResults = [];
let myWatchlist = [];

// ── Calendar State ──────────────────────────────────────────────
// watchedData maps "YYYY-MM-DD" -> total minutes watched that day
let watchedData = {};
let calViewDate = new Date(); // which month/year is shown

document.addEventListener('DOMContentLoaded', function() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');

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

    // Manual entry
    const toggleManualBtn = document.getElementById('toggleManualBtn');
    const manualAddForm = document.getElementById('manualAddForm');
    if (toggleManualBtn && manualAddForm) {
        toggleManualBtn.addEventListener('click', function() {
            const isOpen = manualAddForm.style.display === 'flex';
            manualAddForm.style.display = isOpen ? 'none' : 'flex';
        });
    }

    const injectManualBtn = document.getElementById('injectManualBtn');
    if (injectManualBtn) {
        injectManualBtn.addEventListener('click', function() {
            const title = document.getElementById('manTitle').value.trim();
            const runtime = parseInt(document.getElementById('manRuntime').value, 10);
            const genre = document.getElementById('manGenre').value.trim();

            if (!title || isNaN(runtime)) {
                alert('Title and Runtime are required.');
                return;
            }

            const movie = { title, runtime_minutes: runtime, genre: genre || 'N/A' };
            addToWatchlist(movie);

            document.getElementById('manTitle').value = '';
            document.getElementById('manRuntime').value = '';
            document.getElementById('manGenre').value = '';
            manualAddForm.style.display = 'none';
        });
    }

    // Calendar nav
    document.getElementById('calPrev').addEventListener('click', function () {
        calViewDate.setMonth(calViewDate.getMonth() - 1);
        renderCalendar();
    });

    document.getElementById('calNext').addEventListener('click', function () {
        calViewDate.setMonth(calViewDate.getMonth() + 1);
        renderCalendar();
    });

    document.getElementById('calToday').addEventListener('click', function () {
        calViewDate = new Date();
        renderCalendar();
    });

    loadWatchlistFromServer();
    renderCalendar();
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

    if (!Array.isArray(results) || results.length === 0) {
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
            <button class="add-btn" onclick='addToWatchlist(${JSON.stringify(movie)})'>Add to Watchlist</button>
        `;
        resultsContainer.appendChild(movieDiv);
    });
}

async function addToWatchlist(movie) {
    const todayKey = getTodayKey();
    movie._addedOn = todayKey;
    const mins = movie.runtime_minutes || movie.runtime || 0;
    if (!watchedData[todayKey]) watchedData[todayKey] = { mins: 0, count: 0 };
    watchedData[todayKey].mins += mins;
    watchedData[todayKey].count += 1;

    // Check for excessive watching notification
    const totalMins = watchedData[todayKey].mins;
    if (totalMins > 360) { // 6 hours = 360 minutes
        const hours = Math.floor(totalMins / 60);
        alert(`You've watched ${hours} hours today, go outside, chud`);
    }

    if (movie.ids && movie.ids.simkl_id) {
        // From search, save to server
        try {
            const response = await fetch('http://localhost:8080/watchlist/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(movie)
            });

            const result = await response.json();
            if (!response.ok) {
                console.error('Watchlist save failed:', result);
                // Revert local changes
                watchedData[todayKey].mins -= mins;
                watchedData[todayKey].count -= 1;
                if (watchedData[todayKey].count === 0) delete watchedData[todayKey];
                return;
            }

            await loadWatchlistFromServer();
        } catch (error) {
            console.error('Error saving watchlist entry:', error);
            // Revert
            watchedData[todayKey].mins -= mins;
            watchedData[todayKey].count -= 1;
            if (watchedData[todayKey].count === 0) delete watchedData[todayKey];
        }
    } else {
        // Manual entry, add to local watchlist
        movie.id = Date.now(); // unique id
        myWatchlist.push(movie);
        updateWatchlistDisplay();
        renderCalendar();
    }
}

async function loadWatchlistFromServer() {
    const watchlistContainer = document.getElementById('watchlistContainer');
    watchlistContainer.innerHTML = '<p class="placeholder-text">Loading watchlist...</p>';

    try {
        const response = await fetch('http://localhost:8080/watchlist/load');
        if (!response.ok) {
            throw new Error('Failed to load watchlist');
        }

        const entries = await response.json();
        if (entries.message) {
            watchlistContainer.innerHTML = `<p class="placeholder-text">${entries.message}</p>`;
            return;
        }

        myWatchlist = Array.isArray(entries) ? entries : [];
        // Populate watchedData from loaded entries
        watchedData = {};
        myWatchlist.forEach(movie => {
            const dateKey = movie.date_entered || movie._addedOn || getTodayKey();
            const mins = movie.runtime || 0;
            if (!watchedData[dateKey]) watchedData[dateKey] = { mins: 0, count: 0 };
            watchedData[dateKey].mins += mins;
            watchedData[dateKey].count += 1;
        });
        updateWatchlistDisplay();
        renderCalendar();
    } catch (error) {
        console.error('Error loading watchlist:', error);
        watchlistContainer.innerHTML = '<p class="placeholder-text">Unable to load saved watchlist.</p>';
    }
}

function updateWatchlistDisplay() {
    const watchlistContainer = document.getElementById('watchlistContainer');
    watchlistContainer.innerHTML = '';

    if (myWatchlist.length === 0) {
        watchlistContainer.innerHTML = '<p class="placeholder-text">Your roster is empty.</p>';
        updateTotalTime();
        return;
    }

    myWatchlist.forEach((movie, index) => {
        const movieDiv = document.createElement('div');
        movieDiv.className = 'watchlist-item glass-panel';
        const dateWatched = movie.date_entered || movie._addedOn || 'N/A';
        const posterHtml = movie.poster_url ? `<div class="watchlist-poster"><img src="${movie.poster_url}" alt="${movie.title} poster"></div>` : '';
        movieDiv.innerHTML = `
            <div class="watchlist-details">
                <h3>${movie.title || 'Unknown Title'}</h3>
                <p>Year: ${movie.year || 'N/A'}</p>
                <p>Director: ${movie.director || 'N/A'}</p>
                <p>Runtime: ${movie.runtime || 'N/A'} mins</p>
                <p>Date Watched: ${dateWatched}</p>
                <button class="remove-btn" onclick="removeFromWatchlist(${movie.id || index}, ${movie.simkl_id || 0})">Remove</button>
            </div>
            ${posterHtml}
        `;
        watchlistContainer.appendChild(movieDiv);
    });

    updateTotalTime();
}

function updateTotalTime() {
    const total = myWatchlist.reduce((sum, m) => sum + (m.runtime || 0), 0);
    const h = Math.floor(total / 60);
    const m = total % 60;
    document.getElementById('totalTimeDisplay').textContent = `Total Time: ${h}h ${m}m`;
}

async function removeFromWatchlist(id, simklId) {
    const movie = myWatchlist.find(m => m.id == id);
    if (!movie) return;

    const dateKey = movie.date_entered || movie._addedOn;

    if (dateKey && watchedData[dateKey]) {
        const mins = movie.runtime || 0;
        watchedData[dateKey].mins = Math.max(0, watchedData[dateKey].mins - mins);
        watchedData[dateKey].count = Math.max(0, watchedData[dateKey].count - 1);
        if (watchedData[dateKey].count === 0) delete watchedData[dateKey];
    }

    if (simklId && simklId != 0) {
        // From server
        try {
            const response = await fetch(`http://localhost:8080/watchlist/remove?simkl_id=${encodeURIComponent(simklId)}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                console.error('Failed to remove entry from watchlist');
                // Revert local changes
                if (dateKey) {
                    if (!watchedData[dateKey]) watchedData[dateKey] = { mins: 0, count: 0 };
                    watchedData[dateKey].mins += mins;
                    watchedData[dateKey].count += 1;
                }
                return;
            }
            await loadWatchlistFromServer();
        } catch (error) {
            console.error('Error removing watchlist entry:', error);
            // Revert
            if (dateKey) {
                if (!watchedData[dateKey]) watchedData[dateKey] = { mins: 0, count: 0 };
                watchedData[dateKey].mins += mins;
                watchedData[dateKey].count += 1;
            }
        }
    } else {
        // Manual entry, remove from local
        myWatchlist = myWatchlist.filter(m => m.id != id);
        updateWatchlistDisplay();
        renderCalendar();
    }
}

// ── Calendar ─────────────────────────────────────────────────────
function getTodayKey() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

function renderCalendar() {
    const year = calViewDate.getFullYear();
    const month = calViewDate.getMonth();

    // Update month label
    document.getElementById('calMonthLabel').textContent =
        new Date(year, month, 1).toLocaleString('default', { month: 'long', year: 'numeric' });

    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = '';

    const today = new Date();
    const firstDayOfWeek = new Date(year, month, 1).getDay(); // 0 = Sun
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    // Empty cells before first day
    for (let i = 0; i < firstDayOfWeek; i++) {
        const empty = document.createElement('div');
        empty.className = 'cal-day cal-empty';
        grid.appendChild(empty);
    }

    // Day cells
    for (let d = 1; d <= daysInMonth; d++) {
        const dateKey = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const data = watchedData[dateKey] || { mins: 0, count: 0 };
        const { mins, count } = data;

        const cell = document.createElement('div');
        cell.className = 'cal-day';

        const isToday =
            d === today.getDate() &&
            month === today.getMonth() &&
            year === today.getFullYear();

        if (isToday) cell.classList.add('today');

        // Color intensity based on show count
        if (count === 1) cell.classList.add('watch-1');
        else if (count === 2) cell.classList.add('watch-2');
        else if (count === 3) cell.classList.add('watch-3');
        else if (count >= 4) cell.classList.add('watch-4');

        cell.innerHTML = `
            <span class="cal-date">${d}</span>
            ${count > 0 ? `<span class="cal-mins">${mins}m</span>` : ''}
        `;

        grid.appendChild(cell);
    }
}
