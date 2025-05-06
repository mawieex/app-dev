// Function to calculate streak
function calculateStreak() {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    if (entries.length === 0) return 0;

    let streak = 0;
    const today = new Date().setHours(0, 0, 0, 0);
    const yesterday = new Date(today - 86400000).setHours(0, 0, 0, 0);
    
    // Sort entries by date, newest first
    entries.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    // Check if there's an entry today
    const lastEntryDate = new Date(entries[0].timestamp).setHours(0, 0, 0, 0);
    if (lastEntryDate === today) {
        streak = 1;
        
        // Count consecutive previous days
        let currentDate = yesterday;
        let i = 1;
        
        while (i < entries.length) {
            const entryDate = new Date(entries[i].timestamp).setHours(0, 0, 0, 0);
            if (entryDate === currentDate) {
                streak++;
                currentDate -= 86400000; // Subtract one day in milliseconds
                i++;
            } else if (entryDate < currentDate) {
                i++;
            } else {
                break;
            }
        }
    }
    
    return streak;
}

// Function to calculate total entries
function calculateTotalEntries() {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    return entries.length;
}

// Function to calculate longest streak
function calculateLongestStreak() {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    if (entries.length === 0) return 0;

    let longestStreak = 0;
    let currentStreak = 1;
    
    // Sort entries by date, oldest first
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    
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
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    if (entries.length === 0) return 0;
    
    const uniqueDays = new Set();
    entries.forEach(entry => {
        const date = new Date(entry.timestamp).setHours(0, 0, 0, 0);
        uniqueDays.add(date);
    });
    
    const daysSinceFirstEntry = Math.ceil((new Date() - new Date(entries[0].timestamp)) / (1000 * 60 * 60 * 24));
    return Math.round((uniqueDays.size / daysSinceFirstEntry) * 100);
}

// Function to update insights display
function updateInsightsDisplay() {
    const currentStreak = calculateStreak();
    const totalEntries = calculateTotalEntries();
    const longestStreak = calculateLongestStreak();
    const completionRate = calculateCompletionRate();
    
    document.getElementById('currentStreak').textContent = currentStreak;
    document.getElementById('totalEntries').textContent = totalEntries;
    document.getElementById('longestStreak').textContent = longestStreak;
    document.getElementById('completionRate').textContent = completionRate + '%';
    
    // Update streak progress bar
    const streakProgress = document.getElementById('streakProgress');
    streakProgress.style.width = (currentStreak / Math.max(longestStreak, 1)) * 100 + '%';
}

// Function to prepare mood data for chart
function prepareMoodData() {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    if (entries.length === 0) return { labels: [], data: [] };

    // Sort entries by date
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

    // Create a map to store average mood per day
    const moodMap = new Map();
    
    entries.forEach(entry => {
        const date = new Date(entry.timestamp).toLocaleDateString();
        const moodValue = getMoodValue(entry.mood);
        
        if (!moodMap.has(date)) {
            moodMap.set(date, { sum: moodValue, count: 1 });
        } else {
            const current = moodMap.get(date);
            moodMap.set(date, { 
                sum: current.sum + moodValue, 
                count: current.count + 1 
            });
        }
    });

    // Convert map to arrays for chart
    const labels = Array.from(moodMap.keys());
    const data = Array.from(moodMap.values()).map(day => 
        Math.round((day.sum / day.count) * 10) / 10
    );

    return { labels, data };
}

// Function to convert mood text to numerical value
function getMoodValue(mood) {
    const moodValues = {
        'Happy': 5,
        'Calm': 4,
        'Neutral': 3,
        'Anxious': 2,
        'Sad': 1
    };
    return moodValues[mood] || 3; // Default to neutral if mood not found
}

// Function to update mood chart
function updateMoodChart() {
    const { labels, data } = prepareMoodData();
    
    // Destroy existing chart if it exists
    if (window.moodChart) {
        window.moodChart.destroy();
    }

    const ctx = document.getElementById('moodChart');
    if (!ctx) {
        console.error('Mood chart canvas not found');
        return;
    }

    // Set canvas size
    ctx.style.width = '100%';
    ctx.style.height = '400px';

    const chartContext = ctx.getContext('2d');
    window.moodChart = new Chart(chartContext, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Mood Over Time',
                data: data,
                borderColor: '#4CAF50',
                backgroundColor: 'rgba(76, 175, 80, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            layout: {
                padding: {
                    left: 10,
                    right: 10,
                    top: 10,
                    bottom: 10
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 5,
                    ticks: {
                        stepSize: 1,
                        callback: function(value) {
                            const moodLabels = {
                                1: 'Sad',
                                2: 'Anxious',
                                3: 'Neutral',
                                4: 'Calm',
                                5: 'Happy'
                            };
                            return moodLabels[value] || value;
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
                            return `Mood: ${context.raw}`;
                        }
                    }
                }
            }
        }
    });
}

// Initialize insights when page loads
document.addEventListener("DOMContentLoaded", function () {
    console.log('DOM loaded, initializing insights...');
    
    // Update insights display
    updateInsightsDisplay();
    
    // Update mood chart
    updateMoodChart();
    
    // Log chart status
    console.log('Chart initialized:', window.moodChart ? 'success' : 'failed');
});

// Add event listener for storage changes to update chart
window.addEventListener('storage', function(e) {
    if (e.key === 'journalEntries') {
        console.log('Journal entries updated, refreshing chart...');
        updateMoodChart();
    }
});

//profile