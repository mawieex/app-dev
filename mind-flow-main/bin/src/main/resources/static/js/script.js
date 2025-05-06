//community

//insights
document.addEventListener("DOMContentLoaded", function () {
    const ctx = document.getElementById('moodChart').getContext('2d');
    const moodChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
            datasets: [{
                label: 'Mood Over Time',
                data: [3, 2, 5, 4, 6, 5, 7],
                borderColor: '#d1e1d4',
                backgroundColor: 'rgba(209, 225, 212, 0.2)',
                borderWidth: 2,
                fill: true
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
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
            loadEntries(); // Refresh the displayed entries
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

            // Clear the form fields
            document.getElementById('journalEntry').value = '';
            document.getElementById('moodSelect').value = '';
            document.getElementById('tags').value = '';

            // Reload the entries to display the new one
            loadEntries();
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