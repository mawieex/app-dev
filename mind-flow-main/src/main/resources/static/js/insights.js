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
    console.log('Preparing mood data...');
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    console.log('Journal entries:', entries);
    
    if (entries.length === 0) {
        console.log('No journal entries found');
        const canvas = document.getElementById('moodChart');
        if (canvas) {
            canvas.innerHTML = '<div class="alert alert-info">No journal entries found. Start journaling to see your mood trends!</div>';
        }
        return { labels: [], data: [] };
    }

    // Sort entries by date
    entries.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

    // Create a map to store average mood per day
    const moodMap = new Map();
    
    entries.forEach(entry => {
        const date = new Date(entry.timestamp).toLocaleDateString();
        const moodValue = getMoodValue(entry.mood);
        console.log(`Processing entry - Date: ${date}, Mood: ${entry.mood}, Value: ${moodValue}`);
        
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

    console.log('Prepared chart data:', { labels, data });
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
    console.log('Updating mood chart...');
    
    const { labels, data } = prepareMoodData();
    console.log('Chart data:', { labels, data });
    
    // If no data, don't try to create chart
    if (labels.length === 0 || data.length === 0) {
        console.log('No data to display in chart');
        return;
    }
    
    // Destroy existing chart if it exists
    if (window.moodChart && typeof window.moodChart.destroy === 'function') {
        console.log('Destroying existing chart');
        window.moodChart.destroy();
        window.moodChart = null;
    }

    const canvas = document.getElementById('moodChart');
    if (!canvas) {
        console.error('Mood chart canvas not found');
        return;
    }
    console.log('Found canvas element');

    // Set canvas size
    canvas.style.width = '100%';
    canvas.style.height = '100%';

    const ctx = canvas.getContext('2d');
    if (!ctx) {
        console.error('Could not get canvas context');
        return;
    }
    console.log('Got canvas context');

    try {
        console.log('Creating new chart...');
        // Create new chart instance
        const chartConfig = {
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
        };

        // Create the chart and store it in window.moodChart
        window.moodChart = new Chart(ctx, chartConfig);
        console.log('Chart created successfully');
    } catch (error) {
        console.error('Error creating chart:', error);
        // Show error message in canvas
        canvas.innerHTML = '<div class="alert alert-danger">Error creating chart. Please try refreshing the page.</div>';
        // Clear the error state
        window.moodChart = null;
    }
}

// Function to prepare monthly emotions data
function prepareMonthlyEmotionsData() {
    console.log('Preparing monthly emotions data...');
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    console.log('Total entries:', entries.length);
    
    if (entries.length === 0) {
        console.log('No journal entries found for emotions chart');
        return { labels: [], data: [] };
    }

    // Get current month's entries
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    console.log('Current month:', currentMonth, 'Current year:', currentYear);
    
    const monthlyEntries = entries.filter(entry => {
        const entryDate = new Date(entry.timestamp);
        return entryDate.getMonth() === currentMonth && entryDate.getFullYear() === currentYear;
    });
    console.log('Monthly entries:', monthlyEntries.length);

    // Count emotions
    const emotionCounts = {
        'Happy': 0,
        'Calm': 0,
        'Neutral': 0,
        'Anxious': 0,
        'Sad': 0
    };

    monthlyEntries.forEach(entry => {
        console.log('Processing entry:', entry);
        if (emotionCounts.hasOwnProperty(entry.mood)) {
            emotionCounts[entry.mood]++;
        }
    });

    console.log('Emotion counts:', emotionCounts);
    return {
        labels: Object.keys(emotionCounts),
        data: Object.values(emotionCounts)
    };
}

// Function to prepare activity patterns data
function prepareActivityPatternsData() {
    console.log('Preparing activity patterns data...');
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    console.log('Total entries:', entries.length);
    
    if (entries.length === 0) {
        console.log('No journal entries found for activity patterns chart');
        return { labels: [], data: [] };
    }

    // Get current month's entries
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    console.log('Current month:', currentMonth, 'Current year:', currentYear);
    
    const monthlyEntries = entries.filter(entry => {
        const entryDate = new Date(entry.timestamp);
        return entryDate.getMonth() === currentMonth && entryDate.getFullYear() === currentYear;
    });
    console.log('Monthly entries:', monthlyEntries.length);

    // Count entries by day of week
    const dayCounts = {
        'Sunday': 0,
        'Monday': 0,
        'Tuesday': 0,
        'Wednesday': 0,
        'Thursday': 0,
        'Friday': 0,
        'Saturday': 0
    };

    monthlyEntries.forEach(entry => {
        const day = new Date(entry.timestamp).toLocaleDateString('en-US', { weekday: 'long' });
        console.log('Entry day:', day);
        dayCounts[day]++;
    });

    console.log('Day counts:', dayCounts);
    return {
        labels: Object.keys(dayCounts),
        data: Object.values(dayCounts)
    };
}

// Function to update monthly trends charts
function updateMonthlyTrendsCharts() {
    console.log('Updating monthly trends charts...');
    
    // Update Emotions Chart
    const emotionsData = prepareMonthlyEmotionsData();
    console.log('Emotions data:', emotionsData);
    
    const emotionsCtx = document.getElementById('emotionsChart');
    console.log('Emotions canvas context:', emotionsCtx);
    
    if (emotionsCtx) {
        // Destroy existing chart if it exists and is a valid Chart instance
        if (window.emotionsChart && typeof window.emotionsChart.destroy === 'function') {
            console.log('Destroying existing emotions chart');
            window.emotionsChart.destroy();
        }
        
        try {
            console.log('Creating new emotions chart...');
            window.emotionsChart = new Chart(emotionsCtx, {
                type: 'doughnut',
                data: {
                    labels: emotionsData.labels,
                    datasets: [{
                        data: emotionsData.data,
                        backgroundColor: [
                            '#4CAF50', // Happy - Green
                            '#2196F3', // Calm - Blue
                            '#9E9E9E', // Neutral - Grey
                            '#FFC107', // Anxious - Yellow
                            '#F44336'  // Sad - Red
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        title: {
                            display: true,
                            text: 'Emotional Distribution'
                        }
                    }
                }
            });
            console.log('Emotions chart created successfully');
        } catch (error) {
            console.error('Error creating emotions chart:', error);
        }
    }

    // Update Activity Patterns Chart
    const activityData = prepareActivityPatternsData();
    console.log('Activity data:', activityData);
    
    const activityCtx = document.getElementById('activitiesChart');
    console.log('Activity canvas context:', activityCtx);
    
    if (activityCtx) {
        // Destroy existing chart if it exists and is a valid Chart instance
        if (window.activityChart && typeof window.activityChart.destroy === 'function') {
            console.log('Destroying existing activity chart');
            window.activityChart.destroy();
        }
        
        try {
            console.log('Creating new activity chart...');
            window.activityChart = new Chart(activityCtx, {
                type: 'bar',
                data: {
                    labels: activityData.labels,
                    datasets: [{
                        label: 'Journal Entries',
                        data: activityData.data,
                        backgroundColor: '#4CAF50',
                        borderColor: '#388E3C',
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
                        },
                        title: {
                            display: true,
                            text: 'Entries by Day of Week'
                        }
                    }
                }
            });
            console.log('Activity chart created successfully');
        } catch (error) {
            console.error('Error creating activity chart:', error);
        }
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
    
    // Update mood chart
    updateMoodChart();
    
    // Update monthly trends charts
    updateMonthlyTrendsCharts();
    
    // Log chart status
    console.log('Charts initialized:', {
        moodChart: window.moodChart ? 'success' : 'failed',
        emotionsChart: window.emotionsChart ? 'success' : 'failed',
        activityChart: window.activityChart ? 'success' : 'failed'
    });
});

// Update the storage event listener to include monthly trends
window.addEventListener('storage', function(e) {
    if (e.key === 'journalEntries') {
        console.log('Journal entries updated, refreshing charts...');
        updateMoodChart();
        updateMonthlyTrendsCharts();
    }
});

//profile