document.addEventListener('DOMContentLoaded', function() {
    console.log("JavaScript loaded and executing.");
    const stars = document.querySelectorAll('#star-rating .star');
    const ratingInput = document.getElementById('rating');
    let currentRating = parseInt(ratingInput.value);

    // 별 시각적 상태 업데이트 함수
    function updateStarVisuals(value) {
        console.log(`updateStarVisuals called with value: ${value}`);
        stars.forEach(star => {
            const starValue = parseInt(star.dataset.value);
            if (starValue <= value) {
                star.classList.add('selected');
                console.log(`Star ${starValue} added 'selected'`);
            } else {
                star.classList.remove('selected');
                console.log(`Star ${starValue} removed 'selected'`);
            }
        });
    }

    stars.forEach(star => {
        star.addEventListener('click', function() {
            const value = parseInt(this.dataset.value);
            ratingInput.value = value;
            currentRating = value; // 현재 선택된 평점 업데이트
            console.log(`Clicked star value: ${value}, currentRating: ${currentRating}`);
            updateStarVisuals(value); // 클릭 시 selected 클래스 적용
        });

        star.addEventListener('mouseover', function() {
            const value = parseInt(this.dataset.value);
            console.log(`Mouseover star value: ${value}`);
            updateStarVisuals(value); // 마우스 오버 시 별 채우기
        });

        star.addEventListener('mouseout', function() {
            console.log(`Mouseout, restoring to currentRating: ${currentRating}`);
            updateStarVisuals(currentRating); // 마우스 아웃 시 현재 선택된 평점으로 복원
        });
    });

    // 초기 로드 시 별 상태 업데이트 (기본값 0이므로 모두 빈 별)
    console.log(`Initial load, currentRating: ${currentRating}`);
    updateStarVisuals(currentRating);

    // 파일 업로드 로직
    const fileInput = document.getElementById('fileInput');
    const uploadResultDiv = document.getElementById('uploadResult');
    const form = document.querySelector('form');

    fileInput.addEventListener('change', function(e) {
        const files = e.target.files;
        if (files.length === 0) {
            return;
        }

        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        fetch('/review/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            console.log('Upload successful:', data);
            uploadResultDiv.innerHTML = ''; // 기존 미리보기 초기화
            data.forEach((fileInfo, index) => {
                const imgPath = `/review/view/${fileInfo.link}`; // 이 부분을 수정했습니다.
                const imgElement = document.createElement('img');
                imgElement.src = imgPath;
                imgElement.alt = fileInfo.fileName;
                imgElement.style.maxWidth = '100px';
                imgElement.style.maxHeight = '100px';
                imgElement.style.margin = '5px';
                uploadResultDiv.appendChild(imgElement);

                // 숨겨진 input 필드 추가하여 DTO에 파일 정보 전송
                // Spring MVC가 List<UploadResultDTO>를 바인딩할 수 있도록 인덱싱된 이름 사용
                const uuidInput = document.createElement('input');
                uuidInput.type = 'hidden';
                uuidInput.name = `uploadFileNames[${index}].uuid`;
                uuidInput.value = fileInfo.uuid;
                form.appendChild(uuidInput);

                const fileNameInput = document.createElement('input');
                fileNameInput.type = 'hidden';
                fileNameInput.name = `uploadFileNames[${index}].fileName`;
                fileNameInput.value = fileInfo.fileName;
                form.appendChild(fileNameInput);

                const imgInput = document.createElement('input');
                imgInput.type = 'hidden';
                imgInput.name = `uploadFileNames[${index}].img`;
                imgInput.value = fileInfo.img; // boolean 값은 "true" 또는 "false" 문자열로 전송
                form.appendChild(imgInput);
            });
        })
        .catch(error => {
            console.error('Upload failed:', error);
            alert('파일 업로드에 실패했습니다.');
        });
    });
});