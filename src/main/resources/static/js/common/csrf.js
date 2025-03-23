// csrf.js

/**
 * CSRF 토큰을 쿠키에서 읽어오는 함수
 * @param {string} name - 쿠키 이름 (기본값: XSRF-TOKEN)
 * @returns {string} - CSRF 토큰 값
 */
export function getCsrfToken(cookieName = 'XSRF-TOKEN') {
    const csrfCookie = document.cookie
        .split('; ')
        .find(row => row.startsWith(`${cookieName}=`));
    return csrfCookie ? decodeURIComponent(csrfCookie.split('=')[1]) : null;
}

/**
 * CSRF 헤더와 토큰을 추가하고 로딩 바를 설정하는 AJAX 설정
 */
export function setupAjaxCsrf() {
    const csrfToken = getCsrfToken();

    if (csrfToken) {
        $.ajaxSetup({
            beforeSend: function (xhr, settings) {
                xhr.setRequestHeader('X-XSRF-TOKEN', csrfToken); // CSRF 토큰 추가

                if (settings.contentType !== false) {
                    xhr.setRequestHeader('Content-Type', 'application/json'); // Content-Type 설정
                }

                // AJAX 요청이 시작될 때 로딩 바 표시
                $("#loading-bar").show();
            },
            complete: function () {
                // AJAX 요청이 완료되면 로딩 바 숨김
                $("#loading-bar").hide();
            }
        });
    } else {
        console.warn("CSRF 토큰이 쿠키에서 발견되지 않았습니다.");
    }
}
