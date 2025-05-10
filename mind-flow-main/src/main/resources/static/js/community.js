document.addEventListener('DOMContentLoaded', function() {
    // Handle like button clicks
    document.querySelectorAll('.like-button').forEach(button => {
        button.addEventListener('click', function() {
            const postId = this.dataset.postId;
            const likeCount = this.querySelector('.like-count');
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            
            fetch('/community/like', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body: `postId=${postId}`
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(count => {
                likeCount.textContent = count;
                this.classList.toggle('liked');
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error liking post. Please try again.');
            });
        });
    });
}); 