// archive.js - Handles search/filter for archived journal entries

document.addEventListener('DOMContentLoaded', function() {
    // Disable filtering: always show all entries
    const entries = document.querySelectorAll('#entriesList .journal-entry');
    entries.forEach(entry => {
        entry.style.display = '';
    });
}); 