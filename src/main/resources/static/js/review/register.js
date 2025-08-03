document.addEventListener('DOMContentLoaded', function() {
    // --- 별점 기능 ---
    const stars = document.querySelectorAll('#star-rating .star');
    const ratingInput = document.getElementById('rating');
    const submitButton = document.getElementById('submitButton');
    let currentRating = parseInt(ratingInput.value) || 0;

    function updateStarVisuals(value) {
        stars.forEach(star => {
            star.classList.toggle('selected', parseInt(star.dataset.value) <= value);
        });
    }

    stars.forEach(star => {
        star.addEventListener('click', function() {
            currentRating = parseInt(this.dataset.value);
            ratingInput.value = currentRating;
            updateStarVisuals(currentRating);
        });
        star.addEventListener('mouseover', function() {
            updateStarVisuals(parseInt(this.dataset.value));
        });
        star.addEventListener('mouseout', function() {
            updateStarVisuals(currentRating);
        });
    });
    updateStarVisuals(currentRating);

    // --- 파일 선택 시 미리보기 기능 ---
    const fileInput = document.getElementById('fileInput');
    const uploadResultDiv = document.getElementById('uploadResult');

    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const files = e.target.files;
            if (!files || files.length === 0) {
                return;
            }

            uploadResultDiv.innerHTML = ''; // 기존 미리보기 삭제

            Array.from(files).forEach(file => {
                if (file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function(event) {
                        const imgElement = document.createElement('img');
                        imgElement.src = event.target.result;
                        imgElement.alt = file.name;
                        imgElement.style.maxWidth = '100px';
                        imgElement.style.maxHeight = '100px';
                        imgElement.style.margin = '5px';
                        uploadResultDiv.appendChild(imgElement);
                    };
                    reader.readAsDataURL(file);
                } else {
                    const fileNameSpan = document.createElement('span');
                    fileNameSpan.textContent = `(파일) ${file.name}`;
                    uploadResultDiv.appendChild(fileNameSpan);
                }
            });
        });
    }

    // 페이지 로드 시 submitButton 활성화
    if (submitButton) {
        submitButton.disabled = false;
    }
});