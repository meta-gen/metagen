import {setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";


$(document).ready(() => {
    setupAjaxCsrf();

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $("#download-template").on("click", function () {
        $.ajax({
            url: '/api/downloadTemplate/tableTemplate',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "테이블설계 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });
});
