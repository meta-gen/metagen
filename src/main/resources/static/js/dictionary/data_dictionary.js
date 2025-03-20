import {setupAjaxCsrf} from "../common/csrf.js";
import {downloadFile} from "../common/common.js";

$(document).ready(function () {
    setupAjaxCsrf();

    /**
     * 공공데이터포털 공통표준용어 가져오기
     */
    $(document).on("click", "#download-data-portal", function () {
        $.ajax({
            url: '/api/downloadTemplate/dataPortal',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
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
    });

    /**
     * 템플릿 다운로드 버튼 클릭 이벤트
     */
    $(document).on("click", "#download-template", function () {
        $.ajax({
            url: '/api/downloadTemplate/template',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
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
     */
    $("#excel-upload").on("click", () => {
        debugger;
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
    };
});

document.addEventListener("DOMContentLoaded", function () {
    if (!window.tableInstances) {
        window.tableInstances = {};  // 여러 개의 DataTable을 저장할 객체
    }

    // 표준 용어 이벤트
    document.getElementById("addStandardTerm")?.addEventListener("click", addStandardTerm);
    document.getElementById("saveStandardTerms")?.addEventListener("click", saveStandardTerms);
    document.getElementById("deleteStandardTerm")?.addEventListener("click", deleteStandardTerm);

    // 표준 단어 이벤트
    document.getElementById("addStandardWord")?.addEventListener("click", addStandardWord);
    document.getElementById("saveStandardWords")?.addEventListener("click", saveStandardWords);
    document.getElementById("deleteStandardWord")?.addEventListener("click", deleteStandardWord);

    // 표준 도메인 이벤트
    document.getElementById("addStandardDomain")?.addEventListener("click", addStandardDomain);
    document.getElementById("saveStandardDomains")?.addEventListener("click", saveStandardDomains);
    document.getElementById("deleteStandardDomain")?.addEventListener("click", deleteStandardDomain);

    // 탭 클릭 시 데이터 로드
    document.querySelectorAll('.nav-link').forEach(tab => {
        tab.addEventListener('click', function () {
            let target = this.getAttribute('data-bs-target');

            if (target === "#standard-terms") {
                window.grid("standardTerms", "/api/getStandardTerms", '');
            } else if (target === "#standard-words") {
                window.grid("standardWords", "/api/getStandardWords", '');
            } else if (target === "#standard-domains") {
                window.grid("standardDomains", "/api/getStandardDomains", '');
            }
        });
    });
});
