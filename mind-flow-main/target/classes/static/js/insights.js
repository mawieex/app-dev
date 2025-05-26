// Sync localStorage with the database
async function syncJournalEntriesFromServer() {
    try {
        // Use the new endpoint that returns all entries for the current user
        const response = await fetch('/api/v1/analytics/journals');
        if (!response.ok) throw new Error('Failed to fetch journal entries from server');
        const entries = await response.json();
        localStorage.setItem('journalEntries', JSON.stringify(entries));
        if (typeof updateInsightsDisplay === 'function') updateInsightsDisplay();
        if (typeof initializeCharts === 'function') initializeCharts();
        if (typeof loadEntries === 'function') loadEntries();
    } catch (err) {
        console.error('Error syncing journal entries:', err);
    }
}

// Call sync on page load
window.addEventListener('load', syncJournalEntriesFromServer);

// Function to safely get data from localStorage
function getLocalStorageData(key, defaultValue = []) {
    try {
        const data = localStorage.getItem(key);
        return data ? JSON.parse(data) : defaultValue;
    } catch (error) {
        console.error(`Error reading ${key} from localStorage:`, error);
        return defaultValue;
    }
}

// Function to calculate streak
function calculateStreak() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return 0;

    // Get all unique days with entries (as yyyy-mm-dd)
    const days = new Set(entries.map(entry => {
        const d = new Date(entry.timestamp);
        return d.getFullYear() + '-' + (d.getMonth() + 1).toString().padStart(2, '0') + '-' + d.getDate().toString().padStart(2, '0');
    }));

    // Start from today, count backwards
    let streak = 0;
    let current = new Date();
    while (true) {
        const key = current.getFullYear() + '-' + (current.getMonth() + 1).toString().padStart(2, '0') + '-' + current.getDate().toString().padStart(2, '0');
        if (days.has(key)) {
            streak++;
            current.setDate(current.getDate() - 1);
        } else {
            break;
        }
    }
    return streak;
}

// Function to calculate total entries
function calculateTotalEntries() {
    // Get the total entries from the server-rendered value
    const totalEntriesElement = document.getElementById('totalEntries');
    if (totalEntriesElement) {
        return parseInt(totalEntriesElement.textContent) || 0;
    }
    return 0;
}

// Function to calculate longest streak
function calculateLongestStreak() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return 0;
    if (entries.length === 1) return 1;
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    let longestStreak = 0;
    let currentStreak = 1;
    for (let i = 1; i < entries.length; i++) {
        const currentDate = new Date(entries[i].timestamp).setHours(0, 0, 0, 0);
        const previousDate = new Date(entries[i-1].timestamp).setHours(0, 0, 0, 0);
        const dayDifference = (currentDate - previousDate) / (1000 * 60 * 60 * 24);
        if (dayDifference === 1) {
            currentStreak++;
        } else if (dayDifference > 1) {
            longestStreak = Math.max(longestStreak, currentStreak);
            currentStreak = 1;
        }
    }
    return Math.max(longestStreak, currentStreak);
}

// Function to calculate completion rate
function calculateCompletionRate() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return 0;
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    if (entries.length === 0) return 0;
    const uniqueDays = new Set();
    entries.forEach(entry => {
        const date = new Date(entry.timestamp).toLocaleDateString();
        uniqueDays.add(date);
    });
    const daysSinceFirstEntry = Math.max(1, Math.ceil((new Date() - new Date(entries[0].timestamp)) / (1000 * 60 * 60 * 24)));
    return Math.round((uniqueDays.size / daysSinceFirstEntry) * 100);
}

// Function to update insights display
function updateInsightsDisplay() {
    try {
        const currentStreak = calculateStreak();
        const totalEntries = calculateTotalEntries();
        const longestStreak = calculateLongestStreak();
        const completionRate = calculateCompletionRate();
        
        // Update DOM elements if they exist
        const elements = {
            'currentStreak': currentStreak,
            'totalEntries': totalEntries,
            'longestStreak': longestStreak,
            'completionRate': completionRate + '%'
        };
        
        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });
        
        // Update streak progress bar
        const streakProgress = document.getElementById('streakProgress');
        if (streakProgress) {
            streakProgress.style.width = (currentStreak / Math.max(longestStreak, 1)) * 100 + '%';
        }
    } catch (error) {
        console.error('Error updating insights display:', error);
    }
}

// Map any mood string to Positive, Neutral, or Negative
function mapMoodToSentiment(mood) {
    const positive = ['happy', 'joyful', 'excited', 'relaxed', 'grateful', 'content', 'proud', 'motivated', 'calm', 'optimistic', 'satisfied', 'hopeful', 'energetic', 'loved', 'peaceful'];
    const negative = ['sad', 'angry', 'stressed', 'anxious', 'tired', 'depressed', 'worried', 'frustrated', 'upset', 'lonely', 'afraid', 'guilty', 'ashamed', 'bored', 'disappointed', 'overwhelmed'];
    const neutral = ['neutral', 'okay', 'meh', 'fine', 'indifferent', 'average', 'so-so'];
    if (!mood) return 'Neutral';
    const m = mood.trim().toLowerCase();
    if (positive.includes(m)) return 'Positive';
    if (negative.includes(m)) return 'Negative';
    if (neutral.includes(m)) return 'Neutral';
    // Default: treat unknown moods as Neutral
    return 'Neutral';
}

function getMoodValue(mood) {
    // Use sentiment mapping
    const sentiment = mapMoodToSentiment(mood);
    const moodValues = {
        'Positive': 3,
        'Neutral': 2,
        'Negative': 1
    };
    return moodValues[sentiment] || 2; // Default to neutral if mood not found
}

// Function to prepare mood data for chart
function prepareMoodData() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return { labels: [], data: [] };
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    if (entries.length === 0) return { labels: [], data: [] };
    const moodMap = new Map();
    entries.forEach(entry => {
        const date = new Date(entry.timestamp).toLocaleDateString();
        const moodValue = getMoodValue(entry.mood); // Now uses sentiment mapping
        if (!moodMap.has(date)) {
            moodMap.set(date, { sum: moodValue, count: 1 });
        } else {
            const current = moodMap.get(date);
            moodMap.set(date, { sum: current.sum + moodValue, count: current.count + 1 });
        }
    });
    const labels = Array.from(moodMap.keys());
    const data = Array.from(moodMap.values()).map(day => Math.round((day.sum / day.count) * 10) / 10);
    return { labels, data };
}

// Function to prepare monthly emotions data
function prepareMonthlyEmotionsData() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return { labels: [], data: [] };
    const emotionCounts = { 'Positive': 0, 'Neutral': 0, 'Negative': 0 };
    entries.forEach(entry => {
        const sentiment = mapMoodToSentiment(entry.mood);
        if (emotionCounts.hasOwnProperty(sentiment)) {
            emotionCounts[sentiment]++;
        }
    });
    return { labels: Object.keys(emotionCounts), data: Object.values(emotionCounts) };
}

// Function to prepare activity patterns data
function prepareActivityPatternsData() {
    const entries = getLocalStorageData('journalEntries');
    if (!entries || entries.length === 0) return { labels: [], data: [] };
    const dayCounts = {
        'Sunday': 0,
        'Monday': 0,
        'Tuesday': 0,
        'Wednesday': 0,
        'Thursday': 0,
        'Friday': 0,
        'Saturday': 0
    };
    // Use all entries, not just current month
    entries.forEach(entry => {
        const day = new Date(entry.timestamp).toLocaleDateString('en-US', { weekday: 'long' });
        dayCounts[day]++;
    });
    return { labels: Object.keys(dayCounts), data: Object.values(dayCounts) };
}

// Function to show/hide chart and message
function showChartOrMessage(canvasId, messageId, hasData, message) {
    const canvas = document.getElementById(canvasId);
    const msg = document.getElementById(messageId);
    if (canvas) canvas.style.display = hasData ? '' : 'none';
    if (msg) {
        msg.style.display = hasData ? 'none' : '';
        msg.textContent = hasData ? '' : message;
    }
}

// Function to initialize charts
function initializeCharts() {
    try {
        // Mood chart
        const moodData = prepareMoodData();
        showChartOrMessage('moodChart', 'moodChartMsg', moodData.labels.length > 0, 'No journal entries found. Start journaling to see your mood trends!');
        if (moodData.labels.length > 0) {
            const moodCtx = document.getElementById('moodChart').getContext('2d');
            if (window.moodChart && typeof window.moodChart.destroy === 'function') window.moodChart.destroy();
            window.moodChart = new Chart(moodCtx, {
                type: 'line',
                data: {
                    labels: moodData.labels,
                    datasets: [{
                        label: 'Mood',
                        data: moodData.data,
                        borderColor: '#A8C3A6',
                        backgroundColor: 'rgba(168, 195, 166, 0.1)',
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 3,
                            ticks: {
                                stepSize: 1,
                                callback: function(value) {
                                    const moods = ['Negative', 'Neutral', 'Positive'];
                                    return moods[value - 1] || '';
                                }
                            }
                        },
                        x: {
                            ticks: {
                                maxRotation: 45,
                                minRotation: 45
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const moods = ['Negative', 'Neutral', 'Positive'];
                                    return `Mood: ${moods[context.raw - 1] || context.raw}`;
                                }
                            }
                        }
                    }
                }
            });
        }
        // Emotions chart
        const emotionsData = prepareMonthlyEmotionsData();
        showChartOrMessage('emotionsChart', 'emotionsChartMsg', emotionsData.labels.some((l, i) => emotionsData.data[i] > 0), 'No emotion data available.');
        if (emotionsData.labels.some((l, i) => emotionsData.data[i] > 0)) {
            const emotionsCtx = document.getElementById('emotionsChart').getContext('2d');
            if (window.emotionsChart && typeof window.emotionsChart.destroy === 'function') window.emotionsChart.destroy();
            window.emotionsChart = new Chart(emotionsCtx, {
                type: 'doughnut',
                data: {
                    labels: emotionsData.labels,
                    datasets: [{
                        data: emotionsData.data,
                        backgroundColor: [
                            '#4CAF50', // Positive - Green
                            '#9E9E9E', // Neutral - Grey
                            '#F44336'  // Negative - Red
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });
        }
        // Activity patterns chart
        const activityData = prepareActivityPatternsData();
        showChartOrMessage('activitiesChart', 'activitiesChartMsg', activityData.labels.some((l, i) => activityData.data[i] > 0), 'No activity data available.');
        if (activityData.labels.some((l, i) => activityData.data[i] > 0)) {
            const activitiesCtx = document.getElementById('activitiesChart').getContext('2d');
            if (window.activityChart && typeof window.activityChart.destroy === 'function') window.activityChart.destroy();
            window.activityChart = new Chart(activitiesCtx, {
                type: 'bar',
                data: {
                    labels: activityData.labels,
                    datasets: [{
                        label: 'Journal Entries',
                        data: activityData.data,
                        backgroundColor: '#A8C3A6',
                        borderColor: '#8BA889',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }
    } catch (error) {
        console.error('Error initializing charts:', error);
    }
}

// Update the DOMContentLoaded event listener
document.addEventListener("DOMContentLoaded", function () {
    console.log('DOM loaded, initializing insights...');
    
    // Check if Chart.js is loaded
    if (typeof Chart === 'undefined') {
        console.error('Chart.js is not loaded!');
        const canvas = document.getElementById('moodChart');
        if (canvas) {
            canvas.innerHTML = `
                <div class="alert alert-danger">
                    <h4>Chart Library Not Found</h4>
                    <p>The chart library (Chart.js) failed to load. This could be due to:</p>
                    <ul>
                        <li>No internet connection</li>
                        <li>CDN access issues</li>
                        <li>Script loading problems</li>
                    </ul>
                    <p>Please try:</p>
                    <ol>
                        <li>Refreshing the page</li>
                        <li>Checking your internet connection</li>
                        <li>If the problem persists, contact support</li>
                    </ol>
                </div>
            `;
        }
        return;
    }
    
    console.log('Chart.js is loaded successfully');
    
    // Update insights display
    updateInsightsDisplay();
    
    // Initialize charts
    initializeCharts();
    
    // Log chart status
    console.log('Charts initialized:', {
        moodChart: window.moodChart ? 'success' : 'failed',
        emotionsChart: window.emotionsChart ? 'success' : 'failed',
        activityChart: window.activityChart ? 'success' : 'failed'
    });
});

// Update the storage event listener
window.addEventListener('storage', function(e) {
    if (e.key === 'journalEntries') {
        console.log('Journal entries updated, refreshing charts...');
        updateInsightsDisplay();
        initializeCharts();
    }
});

//profile