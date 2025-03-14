import {setupAjaxCsrf} from "../common/csrf.js";
import {downloadFile} from "../common/common.js";

$(document).ready(function () {
    setupAjaxCsrf();

    /**
     * 공공데이터포털 공통표준용어 가져오기
     */
    $(document).on("click", "#download-data-portal", function () {
        $.ajax({
            url: '/api/downloadTemplate/dataPortal', // 템플릿 다운로드 URL
            type: 'GET',
            xhrFields: {
                responseType: 'blob'  // 응답을 바이너리(blob) 형식으로 받기
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "공통표준용어.xlsx");
                closeDialog("div");
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
    $(document).on("click", "#download-template", function () {
        $.ajax({
            url: '/api/downloadTemplate/template', // 템플릿 다운로드 URL
            type: 'GET',
            xhrFields: {
                responseType: 'blob'  // 응답을 바이너리(blob) 형식으로 받기
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "템플릿.xlsx");
                closeDialog("div");
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

document.addEventListener("DOMContentLoaded", function () {
    window.openCustomDialog = function () {
        const dialogContent = document.createElement("div");

        const btn1 = document.createElement("button");
        btn1.id = "download-data-portal";
        btn1.classList.add("btn", "btn-primary");
        btn1.textContent = "공통표준용어 다운로드";

        const btn2 = document.createElement("button");
        btn2.id = "download-template";
        btn2.classList.add("btn", "btn-primary");
        btn2.textContent = "템플릿 다운로드";

        dialogContent.appendChild(btn1);
        dialogContent.appendChild(btn2);

        openDialog("div", { title: "템플릿 다운로드", content: dialogContent });
    }
})
