import {setupAjaxCsrf} from "../common/csrf.js";
import {downloadFile} from "../common/common.js";


$(document).ready(function () {
    setupAjaxCsrf();

    /**
     * 공공데이터포털 공통표준용어 가져오기
     */
    $("#download-data-portal").on("click", () => {
        $.ajax({
            url: '/api/downloadTemplate/dataPortal', // 템플릿 다운로드 URL
            type: 'GET',
            xhrFields: {
                responseType: 'blob'  // 응답을 바이너리(blob) 형식으로 받기
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "공통표준용어.xlsx");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    })


    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     * */
    $("#download-template").on("click", () => {
        $.ajax({
            url: '/api/downloadTemplate/template', // 템플릿 다운로드 URL
            type: 'GET',
            xhrFields: {
                responseType: 'blob'  // 응답을 바이너리(blob) 형식으로 받기
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "템플릿.xlsx");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    /**
     * 엑셀업로드 버튼 클릭 이벤트
     * */
    $("#excel-upload").on("click", () => {
        debugger
    });

});
