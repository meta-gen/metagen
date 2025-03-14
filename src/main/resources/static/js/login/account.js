import {setupAjaxCsrf} from "../common/csrf.js";

$(document).ready(function () {
    setupAjaxCsrf();

    // 사용자명 변경 클릭 시
    $('#changeNameBtn').on('click', function (event) {
        event.preventDefault();

        const name = $('#name').val();
        if (name === "") {
            openAlert('사용자명을 입력해주세요.');
            return;
        }

        $.ajax({
            url: '/api/updateName', // 사용자명 변경 URL
            type: 'POST',
            data: JSON.stringify({name}),
            success: function (response) {
                if (response.result === 'success') {
                    openAlert('사용자명이 성공적으로 변경되었습니다.', () => {
                        location.reload(); // 새로고침
                    });

                }
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '사용자명 변경 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    // 비밀번호 변경 클릭 시
    $('#changePasswdBtn').on('click', function (event) {
        event.preventDefault();

        // 폼 요소 선택
        const currentPassword = $('#changedPasswd').val(); // 변경 비밀번호
        const confirmPassword = $('#changedPasswd2').val(); // 비밀번호 확인
        const password = $('#password').val(); // 현재 비밀번호

        // 비밀번호 확인 로직
        if (currentPassword !== confirmPassword) {
            openAlert('비밀번호가 일치하지 않습니다.');
            return;
        }

        if (currentPassword === password) {
            openAlert('동일한 비밀번호로 변경은 불가능합니다.');
            return;
        }

        if (password.trim() === '') {
            openAlert('비밀번호를 입력해주세요.');
            return;
        }

        $.ajax({
            url: '/api/updatePwd', // 비밀번호 변경 URL
            type: 'POST',
            data: JSON.stringify({
                password,
                currentPassword
            }),
            success: function (response) {
                if (response.result === 'success') {
                    openAlert('비밀번호가 성공적으로 변경되었습니다.', () => {
                        window.location.href = '/logout';
                    });
                }
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '비밀번호 변경 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });
});
