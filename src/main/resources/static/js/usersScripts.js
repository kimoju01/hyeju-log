/* 로그인 버튼 클릭 이벤트 */
$(document).ready(function () {
    // Submit Button 클릭 시 처리할 함수
    $('#loginSubmitButton').click(function () {
        // 폼 데이터를 JSON 형식으로 변환
        var formData = {
            username: $('#username').val(),
            password: $('#password').val()
        };

        // AJAX 요청 설정
        $.ajax({
            type: 'POST',
            url: '/login',  // 요청 보낼 URL
            contentType: 'application/json',  // 요청 본문의 타입 설정
            data: JSON.stringify(formData),  // JSON으로 변환한 데이터를 문자열로 설정
            success: function (response) {
                // 성공적으로 응답을 받았을 때 처리할 로직
                console.log('로그인 성공:', response);
                window.location.href = '/';
            },
            error: function (xhr, status, error) {
                // 요청 실패 시 처리할 로직
                console.error('로그인 실패:', error);
                alert('로그인에 실패했습니다. 다시 시도해주세요.');
            }
        });
    });
});

/* 회원가입 버튼 클릭 이벤트 */
$(document).ready(function() { // 문서가 준비되면 실행
    $('#regSubmitButton').click(function (event) { // 제출 버튼 클릭 이벤트 핸들러
        event.preventDefault(); // 기본 동작 중단

        var user = { // 사용자 객체 생성
            username: $('#username').val(),
            password: $('#password').val(),
            name: $('#name').val(),
            email: $('#email').val()
        };

        $.ajax({
            type: 'POST', // POST 요청
            url: '/api/users/userreg', // 요청 URL
            contentType: 'application/json', // 요청 데이터 형식
            data: JSON.stringify(user), // JSON 형식으로 변환해 데이터 전송
            success: function (response) { // 성공 콜백
                alert('회원가입이 완료되었습니다.'); // 성공 메시지
                window.location.href = '/loginform'; // 성공 후 리다이렉트
            },
            error: function (xhr) { // 오류 콜백
                // 서버에서 전송한 데이터는 xhr.responseText에 담김.
                var response = JSON.parse(xhr.responseText); // 오류 응답 파싱
                if (xhr.status === 400) { // 400 Bad Request일 경우 (유효성 검사 오류가 있을 경우)
                    response.forEach(error => { // 각 오류 처리
                        if (error.field === 'username') {
                            $('#usernameError').text(error.defaultMessage); // 사용자 이름 오류 메시지 설정
                        } else if (error.field === 'password') {
                            $('#passwordError').text(error.defaultMessage); // 비밀번호 오류 메시지 설정
                        } else if (error.field === 'name') {
                            $('#nameError').text(error.defaultMessage); // 이름 오류 메시지 설정
                        } else if (error.field === 'email') {
                            $('#emailError').text(error.defaultMessage); // 이메일 오류 메시지 설정
                        }
                    });
                } else if (xhr.status === 409) { // 409 Conflict일 경우 (중복 아이디, 이메일일 경우)
                    alert(response.message);
                } else {
                    alert('회원가입 중 오류가 발생했습니다.');
                }
            }
        });
    });
});
