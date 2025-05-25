// archive.js - Handles search/filter for archived journal entries

document.addEventListener('DOMContentLoaded', function() {
    const moodFilterInput = document.getElementById('moodFilterInput');
    const moodFilterBtn = document.getElementById('moodFilterBtn');

    function filterEntries() {
        const filter = moodFilterInput.value.trim().toLowerCase();
        const entries = document.querySelectorAll('#entriesList .journal-entry');
        entries.forEach(entry => {
            const moodElem = entry.querySelector('.mood');
            const contentElem = entry.querySelector('.content');
            const tagElems = entry.querySelectorAll('.tags .tag');
            const mood = moodElem ? moodElem.textContent.trim().toLowerCase() : '';
            const content = contentElem ? contentElem.textContent.trim().toLowerCase() : '';
            let tags = '';
            tagElems.forEach(tag => { tags += tag.textContent.trim().toLowerCase() + ' '; });
            if (!filter || mood.includes(filter) || content.includes(filter) || tags.includes(filter)) {
                entry.style.display = '';
            } else {
                entry.style.display = 'none';
            }
        });
    }
    if (moodFilterInput) {
        moodFilterInput.addEventListener('input', filterEntries);
    }
    if (moodFilterBtn) {
        moodFilterBtn.addEventListener('click', filterEntries);
    }
}); 