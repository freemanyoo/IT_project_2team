document.addEventListener('DOMContentLoaded', function() {
    const mainImage = document.getElementById('mainImage');
    const prevButton = document.querySelector('.prev-button');
    const nextButton = document.querySelector('.next-button');
    const thumbnails = document.querySelectorAll('.thumbnail');

    // uploadFileNames는 Thymeleaf에서 주입된 전역 변수
    if (!mainImage || thumbnails.length === 0 || !uploadFileNames || uploadFileNames.length === 0) {
        // 이미지가 없거나 갤러리 요소가 없으면 스크립트 실행 중단
        return;
    }

    let currentIndex = 0;

    function updateImage(index) {
        if (index < 0) {
            currentIndex = uploadFileNames.length - 1;
        } else if (index >= uploadFileNames.length) {
            currentIndex = 0;
        } else {
            currentIndex = index;
        }

        // 메인 이미지는 원본 링크 사용
        mainImage.src = `/view/${uploadFileNames[currentIndex].link}`;

        // 활성 썸네일 표시 업데이트
        thumbnails.forEach((thumb, i) => {
            if (i === currentIndex) {
                thumb.classList.add('active-thumbnail');
            } else {
                thumb.classList.remove('active-thumbnail');
            }
        });
    }

    prevButton.addEventListener('click', () => {
        updateImage(currentIndex - 1);
    });

    nextButton.addEventListener('click', () => {
        updateImage(currentIndex + 1);
    });

    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const index = parseInt(this.dataset.index);
            updateImage(index);
        });
    });

    // 초기 이미지 설정 (첫 번째 썸네일이 활성화되어 있도록)
    updateImage(0);
});