// csrf.js

/**
 * CSRF 토큰을 쿠키에서 읽어오는 함수
 * @param {string} name - 쿠키 이름 (기본값: XSRF-TOKEN)
 * @returns {string} - CSRF 토큰 값
 */
export function getCsrfToken(name = 'XSRF-TOKEN') {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return '';
}

/**
 * CSRF 헤더와 토큰을 추가하는 AJAX 설정
 */
export function setupAjaxCsrf() {
    debugger
    const csrfToken = getCsrfToken();
    if (csrfToken) {
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader('X-XSRF-TOKEN', csrfToken);
            }
        });
    } else {
        console.warn("CSRF 토큰이 쿠키에서 발견되지 않았습니다.");
    }
}
