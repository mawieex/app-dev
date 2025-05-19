// Show success/error messages (SweetAlert2)
window.addEventListener('DOMContentLoaded', function() {
  const success = window.successMsg || null;
  if (success) {
    Swal.fire({
      title: 'Success!',
      text: success,
      icon: 'success',
      confirmButtonText: 'OK'
    });
  }
  const error = window.errorMsg || null;
  if (error) {
    Swal.fire({
      title: 'Error!',
      text: error,
      icon: 'error',
      confirmButtonText: 'OK'
    });
  }

  // CSRF token and header for AJAX requests
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  // Scroll to journal entries if searchMood param exists
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has('searchMood')) {
    const entriesSection = document.getElementById('journalEntries');
    if (entriesSection) {
      entriesSection.scrollIntoView({ behavior: 'smooth' });
    }
  }

  // Instant Mood/Content/Tags Filter
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

  // AI Mood Suggestion Feature
  const contentInput = document.getElementById('content');
  const moodSelect = document.getElementById('mood');
  const moodSuggestion = document.getElementById('moodSuggestion');
  let aiSuggestTimeout = null;
  if (contentInput && moodSuggestion && moodSelect) {
    contentInput.addEventListener('input', function() {
      clearTimeout(aiSuggestTimeout);
      const text = this.value;
      if (text.length < 5) {
        moodSuggestion.style.display = 'none';
        moodSuggestion.innerHTML = '';
        return;
      }
      aiSuggestTimeout = setTimeout(() => {
        fetch('/api/mood-suggest', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
          },
          body: JSON.stringify({ text })
        })
        .then(res => res.json())
        .then(data => {
          if (data.mood && data.mood.length < 20) {
            moodSuggestion.innerHTML = `<span style="background:#023020;color:#fff;padding:8px 16px;border-radius:16px;cursor:pointer;font-size:1rem;">AI Suggests: <b>${data.mood}</b> (click to use)</span>`;
            moodSuggestion.style.display = '';
            moodSuggestion.onclick = function() {
              moodSelect.value = data.mood;
              moodSuggestion.style.display = 'none';
            };
          } else {
            moodSuggestion.style.display = 'none';
            moodSuggestion.innerHTML = '';
          }
        });
      }, 600); // Debounce to avoid too many API calls
    });
  }
}); 