// Function to load journal entries from local storage
function loadEntries() {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    const entryList = document.getElementById('entryList');
    entryList.innerHTML = ''; // Clear existing entries

    entries.forEach((entry, index) => {
        const newEntry = document.createElement('li');
        newEntry.className = 'list-group-item';
        newEntry.innerHTML = `
            <strong>${entry.timestamp}</strong>: ${entry.journalEntry} <br>
            <em>Mood: ${entry.mood}</em> <br>
            <small>Tags: ${entry.tags}</small>
            <button class="btn btn-danger btn-sm float-right" onclick="deleteEntry(${index})">Delete</button>
        `;
        entryList.appendChild(newEntry);
    });

    // Update streak after loading entries
    updateStreakDisplay();
}

// Function to delete an entry
function deleteEntry(index) {
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    entries.splice(index, 1); // Remove the entry at the specified index
    // Update local storage
    localStorage.setItem('journalEntries', JSON.stringify(entries));
    loadEntries(); // Refresh the displayed entries
}

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

// Function to update streak display
function updateStreakDisplay() {
    const streak = calculateStreak();
    const streakDisplay = document.getElementById('streakDisplay');
    if (streakDisplay) {
        streakDisplay.textContent = `Current Streak: ${streak} day${streak !== 1 ? 's' : ''}`;
    }
}

// Load entries when the page loads
window.onload = function() {
    loadEntries();
    updateStreakDisplay();
};

document.getElementById('journalForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the form from submitting normally

    // Get the values from the form
    const journalEntry = document.getElementById('journalEntry').value;
    const mood = document.getElementById('moodSelect').value;
    const tags = document.getElementById('tags').value;

    // Get the current date and time
    const now = new Date();
    const timestamp = now.toLocaleString(); // Format the date and time

    // Create a new journal entry object
    const newEntry = {
        timestamp: timestamp,
        journalEntry: journalEntry,
        mood: mood,
        tags: tags
    };

    // Retrieve existing entries from local storage
    const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
    entries.push(newEntry); // Add the new entry to the array

    // Save the updated entries back to local storage
    localStorage.setItem('journalEntries', JSON.stringify(entries));

    // Clear the form fields
    document.getElementById('journalEntry').value = '';
    document.getElementById('moodSelect').value = '';
    document.getElementById('tags').value = '';

    // Reload the entries to display the new one
    loadEntries();
});
