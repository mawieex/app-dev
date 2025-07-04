//community

//insights
document.addEventListener("DOMContentLoaded", function () {
    // Chart initialization is now handled in insights.js
});

//journal
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
        }

        // Function to delete an entry
        function deleteEntry(index) {
            const entries = JSON.parse(localStorage.getItem('journalEntries')) || [];
            entries.splice(index, 1); // Remove the entry at the specified index
            // Update local storage
            localStorage.setItem('journalEntries', JSON.stringify(entries));
            if (typeof updateInsightsDisplay === 'function') updateInsightsDisplay();
            if (typeof initializeCharts === 'function') initializeCharts();
            loadEntries(); // Refresh the displayed entries
            syncJournalEntriesFromServer();
        }

        // Load entries when the page loads
        window.onload = loadEntries;

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
            if (typeof updateInsightsDisplay === 'function') updateInsightsDisplay();
            if (typeof initializeCharts === 'function') initializeCharts();

            // Clear the form fields
            document.getElementById('journalEntry').value = '';
            document.getElementById('moodSelect').value = '';
            document.getElementById('tags').value = '';

            // Reload the entries to display the new one
            loadEntries();
            syncJournalEntriesFromServer();
        });
        document.addEventListener("DOMContentLoaded", function () {
            fetch("/user/session")
                .then(response => response.json())
                .then(data => {
                    if (data.email) {
                        document.getElementById("userEmail").innerText = data.email;
                    } else {
                        window.location.href = "login.html"; // Redirect if not logged in
                    }
                });
            
            document.getElementById("logoutButton").addEventListener("click", function () {
                fetch("/user/logout", { method: "POST" })
                    .then(() => window.location.href = "login.html");
            });
        });
        document.getElementById("loginForm").addEventListener("submit", function(event) {
            event.preventDefault(); // Prevent default form submission
            
            const email = document.getElementById("email").value;
            const password = document.getElementById("password").value;

            fetch("/user/authenticate", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email: email, password: password })
            })
            .then(response => {
                if (response.ok) {
                    window.location.href = "journal.html"; // Redirect to journaling page
                } else {
                    alert("Invalid email or password!");
                }
            })
            .catch(error => console.error("Error:", error));
        });
   
//login
document.getElementById("loginForm").addEventListener("submit", function (event) {
       event.preventDefault(); // Prevent default form submission

       const formData = new FormData(this);
       fetch("/user/authenticate", {
           method: "POST",
           body: formData
       })
       .then(response => {
           if (response.ok) {
               window.location.href = "journal.html"; // Redirect to journal page
           } else {
               alert("Invalid email or password. Please try again.");
           }
       })
       .catch(error => console.error("Error:", error));
   });
//profile
//register
//security

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

window.addEventListener('DOMContentLoaded', function() {
    syncJournalEntriesFromServer();
});