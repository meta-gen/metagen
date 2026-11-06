import {getCsrfToken, setupAjaxCsrf} from "../../common/csrf.js";
import {downloadFile} from "../../common/common.js";

$(document).ready(function () {
    setupAjaxCsrf();

    /**
     * 상단 버튼(템플릿 다운로드/업로드)을 활성 탭 그리드의 버튼 그룹으로 이동
     */
    $('a[data-bs-toggle="tab"]').on("shown.bs.tab", function () {
        const tableId = $($(this).data("bs-target")).find(".grid-header [data-table-id]").first().data("table-id");
        window.moveGridExtraActions($(".grid-extra-actions"), tableId);
    });

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
                downloadFile(blob, status, xhr, "데이터사전 템플릿.xlsx");
                closeDialog("div");
            },
            error: function (xhr) {
                const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                openAlert(errorMessage);
            }
        });
    });

    /**
     * 데이터사전 다운로드 버튼 클릭 이벤트
     */
    $(document).on("click", "#download-dictionary", function () {
        $.ajax({
            url: '/api/downloadTemplate/dictionary',
            type: 'GET',
            xhrFields: {
                responseType: 'blob'
            },
            success: function (blob, status, xhr) {
                downloadFile(blob, status, xhr, "데이터 사전.xlsx");
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
    $("#excel-upload").on("click", function () {
        $("#upload-file").click();
    });

    /**
     * 엑셀 파일이 업로드 된 이후 서버에 전송 및 파싱처리
     */
    $("#upload-file").on("change", () => {
        window.openConfirm("이 프로젝트에서 데이터 사전 약어 사용 여부가 ‘사용 안 함’으로 설정되지 않은 경우, 해당 표준단어 영문명이 여러 단어로 구성되어 있어 약어가 자동 적용됩니다. 계속 진행하시겠습니까?", () => {
            const file = $("#upload-file")[0].files[0]; // 선택한 파일 가져오기
            if (!file) {
                alert("업로드할 파일을 선택해주세요.");
                return;
            }

            // FormData 생성 후 파일 추가
            const formData = new FormData();
            formData.append("file", file);

            for (let pair of formData.entries()) {
                console.log(pair[0] + ', ' + pair[1]);
            }

            $.ajax({
                url: "/api/uploadDataDictionaryExcelFile",
                type: "POST",
                data: formData,
                processData: false,  // FormData를 쿼리 스트링으로 변환하지 않음
                contentType: false,  // `multipart/form-data`가 자동 설정되도록 false로 설정
                beforeSend: function (xhr) {  // setupAjaxCsrf() 영향 방지
                    xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken()); // CSRF 토큰만 추가
                },
                success: function (response) {
                    if(response.result === "success"){
                        window.openAlert("업로드가 완료되었습니다.");
                        ["standardDomains","standardTerms","standardWords"].forEach(e => {
                            $(`.search-input[data-table-id="${e}"]`).val('');
                            window.searchGrid(e);
                        })
                        $("#upload-file").val('');
                    }
                },
                error: function (xhr, status, error) {
                    const errorMessage = xhr.responseJSON?.message || '파일 다운로드 중 문제가 발생했습니다.';
                    openAlert(errorMessage);
                }
            });
        });
    });

    /**
     *
     * 템플릿 다운로드 모달 열기
     */
    window.openCustomDialog = function () {
        const icon = '<svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true"><path fill="currentColor" d="M2.75 14A1.75 1.75 0 0 1 1 12.25v-2.5a.75.75 0 0 1 1.5 0v2.5c0 .138.112.25.25.25h10.5a.25.25 0 0 0 .25-.25v-2.5a.75.75 0 0 1 1.5 0v2.5A1.75 1.75 0 0 1 13.25 14Z"/><path fill="currentColor" d="M7.25 7.689V2a.75.75 0 0 1 1.5 0v5.689l1.97-1.969a.749.749 0 1 1 1.06 1.06l-3.25 3.25a.749.749 0 0 1-1.06 0L4.22 6.78a.749.749 0 1 1 1.06-1.06l1.97 1.969Z"/></svg>';

        const dialogContent = $('<div class="gh-action-list"></div>');

        const btn1 = $('<button type="button" id="download-data-portal"></button>')
            .html(`${icon}<div><strong>공통표준용어 다운로드</strong><span>공공데이터포털의 공통표준용어 파일을 내려받습니다.</span></div>`);

        const btn2 = $('<button type="button" id="download-template"></button>')
            .html(`${icon}<div><strong>빈 템플릿 다운로드</strong><span>데이터사전 업로드용 엑셀 양식을 내려받습니다.</span></div>`);

        dialogContent.append(btn1, btn2);
        openDialog("div", {title: "템플릿 다운로드", content: dialogContent});
    };

    /**
     * DataTable 전역 객체 초기화
     */
    if (!window.tableInstances) {
        window.tableInstances = {};  // 여러 개의 DataTable을 저장할 객체
    }

    /**
     * 탭 클릭 시 해당 탭의 데이터 로드
     */
    $(".nav-link").on("click", function () {
        let target = $(this).attr("data-bs-target");

        if (target === "#standard-terms") {
            window.grid("standardTerms", "/api/getStandardTerms", '');
        } else if (target === "#standard-words") {
            window.grid("standardWords", "/api/getStandardWords", '');
        } else if (target === "#standard-domains") {
            window.grid("standardDomains", "/api/getStandardDomains", '');
        }
    });
});
